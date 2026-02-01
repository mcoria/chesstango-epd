package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;
import net.chesstango.epd.worker.EpdSearchRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.chesstango.epd.master.Common.createSessionId;
import static net.chesstango.epd.master.Common.listEpdFiles;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMainProducer implements Runnable {
    /**
     * Parametros
     * 1. Depth
     * 2. TimeOut in milliseconds
     * 3. Directorio donde se encuentran los archivos de posicion
     * 4. Filtro de archivos
     * <p>
     * Ejemplo:
     * 4 500 C:\java\projects\chess\chess-utils\testing\positions\database "(mate-[wb][123].epd|Bratko-Kopec.epd|Kaufman.epd|wac-2018.epd|STS*.epd|Nolot.epd|sbd.epd)"
     *
     * <p>
     *     Ejecutar VM con
     *     -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     * @param args
     */
    public static void main(String[] args) {
        int depth = Integer.parseInt(args[0]);

        int timeOut = Integer.parseInt(args[1]);

        String directory = args[2];

        String filePattern = args[3];

        System.out.printf("depth={%d}; timeOut={%d}; directory={%s}; filePattern={%s}\n", depth, timeOut, directory, filePattern);

        Path suiteDirectory = Path.of(directory);
        if (!Files.exists(suiteDirectory) || !Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        String sessionId = createSessionId(depth);

        List<Path> epdFiles = listEpdFiles(suiteDirectory, filePattern);

        new EpdSearchMainProducer(sessionId, epdFiles, depth, timeOut).run();
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

        List<EpdSearchRequest> epdSearchRequests = createEpdSearchRequests();

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setSharedExecutor(executorService);

            try (EpdSearchProducer epdSearchProducer = new EpdSearchProducer(factory)) {
                epdSearchRequests.forEach(epdSearchProducer::publish);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Finished");
    }


    private List<EpdSearchRequest> createEpdSearchRequests() {
        List<EpdSearchRequest> epdSearchRequests = new LinkedList<>();

        EPDDecoder reader = new EPDDecoder();
        for (Path epdFile : epdFiles) {
            log.info("Reading {}", epdFile.getFileName());

            List<EPD> edpEntries = reader.readEpdFile(epdFile);

            EpdSearchRequest epdSearchRequest = new EpdSearchRequest()
                    .setSessionId(sessionId)
                    .setSearchId(epdFile.getFileName().toString())
                    .setEpdList(edpEntries)
                    .setDepth(depth)
                    .setTimeOut(timeOut);

            epdSearchRequests.add(epdSearchRequest);
        }
        return epdSearchRequests;
    }

}
