package net.chesstango.epd.core.main;

import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.EpdSearchResultBuildWithBestMove;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;
import net.chesstango.search.builders.AlphaBetaBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;



/**
 * Esta clase esta destinada a resolver test-positions
 * <p>
 * https://www.chessprogramming.org/Test-Positions
 *
 * @author Mauricio Coria
 */
public class EpdSearchMain implements Runnable {

    /**
     * Parametros
     * 1. Depth
     * 2. TimeOut in milliseconds
     * 3. Directorio donde se encuentran los archivos de posicion
     * 4. Filtro de archivos
     * <p>
     * Ejemplo:
     * 4 500 C:\java\projects\chess\chess-utils\testing\positions\database "(mate-[wb][123].epd|Bratko-Kopec.epd|wac-2018.epd|STS*.epd|Nolot.epd|sbd.epd)"
     *
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

        List<Path> epdFiles = Common.listEpdFiles(suiteDirectory, filePattern);

        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, depth);

        new EpdSearchMain(epdFiles, depth, timeOut, sessionDirectory).run();
    }

    private final List<Path> epdFiles;
    private final int depth;
    private final int timeOut;
    private final EpdSearchReportSaver epdSearchReportSaver;

    public EpdSearchMain(List<Path> epdFiles, int depth, int timeOut, Path sessionDirectory) {
        this.epdFiles = epdFiles;
        this.depth = depth;
        this.timeOut = timeOut;
        this.epdSearchReportSaver = new EpdSearchReportSaver(sessionDirectory);
    }

    @Override
    public void run() {
        EpdSearch epdSearch = new EpdSearch()
                .setSearchSupplier(() -> AlphaBetaBuilder
                        .createDefaultBuilderInstance()
                        // Hasta v0.0.27 se utilizó EvaluatorSEandImp02
                        // (ahora EvaluatorImp04) como evaluador
                        .withGameEvaluator(Evaluator.getInstance())
                        .withStatistics()
                        .build())
                .setDepth(depth)
                .setEpdSearchResultBuilder(new EpdSearchResultBuildWithBestMove());


        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        for (Path epdFile : epdFiles) {
            EPDDecoder reader = new EPDDecoder();

            List<EPD> edpEntries = reader.readEpdFile(epdFile);

            List<EpdSearchResult> epdSearchResults = epdSearch.run(edpEntries);

            epdSearchReportSaver.saveReport(epdFile.getFileName().toString(), epdSearchResults);
        }

    }
}
