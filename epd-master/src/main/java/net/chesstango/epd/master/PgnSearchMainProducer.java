package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.worker.PgnSearchRequest;
import net.chesstango.epd.worker.SearchRequest;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNDecoder;

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
public class PgnSearchMainProducer implements Runnable {
    /**
     * Parametros
     * 1. Archivo PGN
     * <p>
     * Ejemplo:
     * C:\java\projects\chess\chess-utils\testing\PGN\database depth-5.pgn
     *
     * <p>
     * Ejecutar VM con
     * -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        String directory = args[0];

        String fileName = args[1];

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

            new PgnSearchMainProducer(sessionId, pgnList)
                    .run();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String rabbitHost;
    private final String sessionId;
    private final List<PGN> pgnList;

    public PgnSearchMainProducer(String sessionId, List<PGN> pgnList) {
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

            try (EpdSearchProducer epdSearchProducer = new EpdSearchProducer(factory)) {
                searchRequests.forEach(epdSearchProducer::publish);
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
}
