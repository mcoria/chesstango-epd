package net.chesstango.epd.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.main.EpdSearchReportSaver;
import net.chesstango.epd.worker.EpdSearchResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMainReader {

    public static void main(String[] args) {
        Path sessionDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\depth-6-2026-01-01-11-32-v1.2.0-SNAPSHOT");

        Stream<EpdSearchResponse> matchResponses = loadEpdSearchResponse(sessionDirectory);

        EpdSearchReportSaver epdSearchReportSaver = new EpdSearchReportSaver(sessionDirectory);

        matchResponses.forEach(epdSearchResponse -> {
            log.info("Saving report {}", epdSearchResponse.getSearchId());
            epdSearchReportSaver.saveReport(epdSearchResponse.getSearchId(), epdSearchResponse.getEpdSearchResults());
        });

    }

    private static Stream<EpdSearchResponse> loadEpdSearchResponse(Path sessionDirectory) {
        File directory = sessionDirectory.toFile();

        log.info("Loading EpdSearchResponse from {}", directory.getAbsolutePath());

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ser"));

        log.info("Found {} ", Arrays.toString(files));

        assert files != null;
        return Stream.of(files).map(file -> {
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
