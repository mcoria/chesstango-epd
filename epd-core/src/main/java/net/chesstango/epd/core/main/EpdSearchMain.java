package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.EpdSearchReportSaver;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.SearchSupplier;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static net.chesstango.epd.core.main.Common.createSessionId;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMain implements Runnable {
    /**
     * Parametros
     * 1. Depth
     * 2. TimeOut in milliseconds
     * 3. Directorio donde se encuentran los archivos de posicion
     * 4. Filtro de archivos
     * <p>
     * Ejemplo:
     * 6 0 C:\java\projects\chess\chess-utils\testing\EPD\database "(mate-[wb][123].epd|Bratko-Kopec.epd|Kaufman.epd|wac-2018.epd|STS*.epd|Nolot.epd|sbd.epd)"
     *
     * <p>
     * Ejecutar VM con
     * -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        int depth = Integer.parseInt(args[0]);

        int timeOut = Integer.parseInt(args[1]);

        String directory = args[2];

        String filePattern = args[3];

        System.out.printf("depth={%d}; timeOut={%d}; directory={%s}; filePattern={%s}%n", depth, timeOut, directory, filePattern);

        Path suiteDirectory = Path.of(directory);
        if (!Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        List<Path> epdFiles = Common.listEpdFiles(suiteDirectory, filePattern);

        String sessionId = createSessionId(depth);

        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, sessionId);

        new EpdSearchMain(sessionId, sessionDirectory, epdFiles, depth, timeOut)
                .run();
    }

    private final List<Path> epdFiles;
    private final int depth;
    private final int timeOut;

    private final EPDDecoder reader;
    private final SearchSupplier searchSupplier;
    private final EpdSearchReportSaver epdSearchReportSaver;

    public EpdSearchMain(String sessionId, Path sessionDirectory, List<Path> epdFiles, int depth, int timeOut) {
        this.epdFiles = epdFiles;
        this.depth = depth;
        this.timeOut = timeOut;
        this.reader = new EPDDecoder();
        this.searchSupplier = new SearchSupplier();
        this.epdSearchReportSaver = new EpdSearchReportSaver(sessionId, sessionDirectory);
    }

    @Override
    public void run() {
        EpdSearch epdSearch = new EpdSearch()
                .setDepth(depth);

        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        for (Path epdFile : epdFiles) {
            try {
                String suiteName = epdFile.getFileName().toString();

                Stream<EPD> edpEntries = reader.decodeEPDs(epdFile);

                List<EpdSearchResult> epdSearchResults = epdSearch.run(searchSupplier, edpEntries);

                epdSearchReportSaver.accept(suiteName, epdSearchResults);

            } catch (IOException ioException) {
                log.error("Error reading file: {}", epdFile, ioException);
            }
        }
    }
}
