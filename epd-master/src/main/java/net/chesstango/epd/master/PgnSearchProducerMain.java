package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.worker.PgnSearchRequest;
import net.chesstango.epd.worker.SearchRequest;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNDecoder;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static net.chesstango.epd.core.main.Common.createSessionId;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class PgnSearchProducerMain implements Runnable {
    /**
     * Parametros
     * -i Directorio donde se encuentra el archivo PGN
     * -f Archivo PGN
     * <p>
     * Ejemplo:
     * -i C:\java\projects\chess\chess-utils\testing\PGN\database -f depth-5.pgn
     *
     * <p>
     * Ejecutar VM con
     * -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        String directory = parsedArgs.getOptionValue('i');

        String fileName = parsedArgs.getOptionValue('f');

        System.out.printf("directory={%s}; file={%s}%n", directory, fileName);

        Path suiteDirectory = Path.of(directory);
        if (!Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        String sessionId = createSessionId(fileName);

        Path pgnFilePath = suiteDirectory.resolve(fileName);

        if (!Files.exists(pgnFilePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }


        PGNDecoder pgnDecoder = new PGNDecoder();

        try (Stream<PGN> pgnStream = pgnDecoder.decodePGNs(pgnFilePath)) {

            List<PGN> pgnList = pgnStream.toList();

            new PgnSearchProducerMain(sessionId, pgnList)
                    .run();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String rabbitHost;
    private final String sessionId;
    private final List<PGN> pgnList;

    public PgnSearchProducerMain(String sessionId, List<PGN> pgnList) {
        this.rabbitHost = "localhost";
        this.sessionId = sessionId;
        this.pgnList = pgnList;
    }

    @Override
    public void run() {
        log.info("Starting");

        List<SearchRequest> searchRequests = createSearchRequests();

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setSharedExecutor(executorService);

            try (SearchProducer searchProducer = new SearchProducer(factory)) {
                searchRequests.forEach(searchProducer::publish);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Finished");
    }


    private List<SearchRequest> createSearchRequests() {
        List<SearchRequest> searchRequests = new LinkedList<>();
        for (PGN pgn : pgnList) {
            String suiteName = pgn.getEvent();
            SearchRequest searchRequest = new PgnSearchRequest()
                    .setPgn(pgn)
                    .setSearchId(suiteName)
                    .setSessionId(sessionId);

            searchRequests.add(searchRequest);
        }
        return searchRequests;
    }

    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();

        Option directoryOpt = Option.builder("i").argName("directory").hasArg().required().desc("directory where pgn file is located").build();
        options.addOption(directoryOpt);

        Option fileNameOpt = Option.builder("f").argName("fileName").hasArg().required().desc("pgn file name").build();
        options.addOption(fileNameOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            new HelpFormatter().printHelp(PgnSearchProducerMain.class.getName(), options);
            System.exit(-1);
        }
        return null;
    }
}
