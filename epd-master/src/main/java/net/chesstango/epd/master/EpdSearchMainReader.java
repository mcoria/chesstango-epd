package net.chesstango.epd.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.EpdSearchReportSaver;
import net.chesstango.epd.worker.EpdSearchResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMainReader {

    public static void main(String[] args) {
        Path sessionDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\depth-5-2026-02-22-09-48-v1.4.0-SNAPSHOT");

        Stream<EpdSearchResponse> epdSearchResponseStream = readlEpdSearchResponses(sessionDirectory);

        epdSearchResponseStream
                .parallel()
                .forEach(epdSearchResponse -> {
                    EpdSearchReportSaver epdSearchReportSaver = new EpdSearchReportSaver(sessionDirectory);

                    epdSearchReportSaver.loadModel(epdSearchResponse.getSessionId(), epdSearchResponse.getEpdSearchResults());

                    CompletableFuture<Void> saveReport = CompletableFuture.supplyAsync(() -> {
                        epdSearchReportSaver.saveReport(epdSearchResponse.getSearchId());
                        return null;
                    });

                    CompletableFuture<Void> saveJson = CompletableFuture.supplyAsync(() -> {
                        epdSearchReportSaver.saveJson(epdSearchResponse.getSearchId());
                        return null;
                    });

                    CompletableFuture<Void> combinedSave = CompletableFuture.allOf(saveReport, saveJson);

                    log.info("Saving reports {}", epdSearchResponse.getSearchId());

                    combinedSave.join();
                });

        log.info("Work completed");
    }

    private static Stream<EpdSearchResponse> readlEpdSearchResponses(Path sessionDirectory) {
        File directory = sessionDirectory.toFile();

        log.info("Loading EpdSearchResponse from {}", directory.getAbsolutePath());

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ser"));

        log.info("Found {} ", Arrays.toString(files));

        assert files != null;

        return Stream
                .of(files)
                .map(file -> {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        log.info("Deserializing file: {}", file.getName());
                        return (EpdSearchResponse) ois.readObject();
                    } catch (Exception e) {
                        log.error("Failed to deserialize file: " + file, e);
                        return null;
                    }
                }).filter(Objects::nonNull);
    }
}
