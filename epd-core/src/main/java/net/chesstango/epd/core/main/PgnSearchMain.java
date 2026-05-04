package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.EpdSearchReportSaver;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.PgnSearch;
import net.chesstango.epd.core.search.SearchSupplier;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNDecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.chesstango.epd.core.main.Common.SESSION_DATE;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class PgnSearchMain implements Runnable {
    /**
     * Parametros
     * 1. Archivo PGN
     * <p>
     * Ejemplo:
     * C:\java\projects\chess\chess-utils\testing\EPD\database games.pgn
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

        Path directoryPath = Path.of(directory);

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        Path pgnFilePath = directoryPath.resolve(fileName);

        if (!Files.exists(pgnFilePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }

        Path sessionDirectory = Common.createSessionDirectory(directoryPath, fileName);

        PGNDecoder pgnDecoder = new PGNDecoder();

        try (Stream<PGN> pgnStream = pgnDecoder.decodePGNs(pgnFilePath)) {

            List<PGN> pgnList = pgnStream.toList();

            new PgnSearchMain(pgnList, sessionDirectory)
                    .run();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private final List<PGN> pgnList;

    private final EpdSearchReportSaver epdSearchReportSaver;

    public PgnSearchMain(List<PGN> pgnList, Path sessionDirectory) {
        this.pgnList = pgnList;
        this.epdSearchReportSaver = new EpdSearchReportSaver(sessionDirectory);
    }

    @Override
    public void run() {
        PgnSearch epdSearch = new PgnSearch();

        for (PGN pgn : pgnList) {

            String suiteName = pgn.getEvent();

            try {

                SearchSupplier searchSupplier = new SearchSupplier();

                List<EpdSearchResult> epdSearchResults = epdSearch.run(searchSupplier, pgn);

                epdSearchReportSaver.loadModel(SESSION_DATE, epdSearchResults);

                CompletableFuture<Void> saveReport = CompletableFuture.supplyAsync(() -> {
                    epdSearchReportSaver.saveReport(suiteName);
                    return null;
                });

                CompletableFuture<Void> saveJson = CompletableFuture.supplyAsync(() -> {
                    epdSearchReportSaver.saveJson(suiteName);
                    return null;
                });

                CompletableFuture<Void> combinedSave = CompletableFuture.allOf(saveReport, saveJson);

                log.info("Saving reports {}", suiteName);

                combinedSave.join();
            } catch (RuntimeException exception) {
                log.error("Error searching: {}", suiteName, exception);
            }
        }
    }
}
