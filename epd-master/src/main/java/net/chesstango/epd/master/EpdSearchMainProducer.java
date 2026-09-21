package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.worker.EpdSearchRequest;
import net.chesstango.epd.worker.SearchRequest;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;
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
import static net.chesstango.epd.core.main.Common.listEpdFiles;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMainProducer implements Runnable {
    /**
     * Parametros
     * -d Depth
     * -t TimeOut in milliseconds
     * -i Directorio donde se encuentran los archivos de posicion
     * -f Filtro de archivos
     * <p>
     * Ejemplo:
     * -d 4 -t 500 -i C:\java\projects\chess\chess-utils\testing\EPD\database -f "(mate-[wb][123].epd|Bratko-Kopec.epd|Kaufman.epd|wac-2018.epd|STS*.epd|Nolot.epd|sbd.epd)"
     *
     * <p>
     * Ejecutar VM con`
     * -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        int depth = Integer.parseInt(parsedArgs.getOptionValue('d'));

        int timeOut = parsedArgs.hasOption('t') ? Integer.parseInt(parsedArgs.getOptionValue('t')) : 0;

        String directory = parsedArgs.getOptionValue('i');

        String filePattern = parsedArgs.getOptionValue('f');

        System.out.printf("depth={%d}; timeOut={%d}; directory={%s}; filePattern={%s}%n", depth, timeOut, directory, filePattern);

        Path suiteDirectory = Path.of(directory);
        if (!Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        String sessionId = createSessionId(depth);

        List<Path> epdFiles = listEpdFiles(suiteDirectory, filePattern);

        new EpdSearchMainProducer(sessionId, epdFiles, depth, timeOut).run();
    }

    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();

        Option depthOpt = Option.builder("d").argName("depth").hasArg().required().desc("search depth").build();
        options.addOption(depthOpt);

        Option timeOutOpt = Option.builder("t").argName("timeOut").hasArg().desc("timeout in milliseconds").build();
        options.addOption(timeOutOpt);

        Option directoryOpt = Option.builder("i").argName("directory").hasArg().required().desc("directory where epd files are located").build();
        options.addOption(directoryOpt);

        Option filePatternOpt = Option.builder("f").argName("filePattern").hasArg().required().desc("epd file name pattern").build();
        options.addOption(filePatternOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            new HelpFormatter().printHelp(EpdSearchMainProducer.class.getName(), options);
            System.exit(-1);
        }
        return null;
    }

    private final String rabbitHost;

    private final String sessionId;
    private final List<Path> epdFiles;
    private final int depth;
    private final int timeOut;


    public EpdSearchMainProducer(String sessionId, List<Path> epdFiles, int depth, int timeOut) {
        this.rabbitHost = "localhost";
        this.sessionId = sessionId;
        this.epdFiles = epdFiles;
        this.depth = depth;
        this.timeOut = timeOut;
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

        EPDDecoder reader = new EPDDecoder();
        for (Path epdFile : epdFiles) {
            try {
                log.info("Reading {}", epdFile.getFileName());

                Stream<EPD> edpEntries = reader.decodeEPDs(epdFile);

                SearchRequest searchRequest = new EpdSearchRequest()
                        .setEpdList(edpEntries.toList())
                        .setDepth(depth)
                        .setTimeOut(timeOut)
                        .setSessionId(sessionId)
                        .setSearchId(epdFile.getFileName().toString());

                searchRequests.add(searchRequest);

            } catch (IOException ioException) {
                log.error("Error reading {}", epdFile.getFileName(), ioException);
            }
        }
        return searchRequests;
    }
}
