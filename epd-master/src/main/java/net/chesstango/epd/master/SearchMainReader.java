package net.chesstango.epd.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.main.SearchReportSaver;
import net.chesstango.epd.worker.SearchResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class SearchMainReader {

    public static void main(String[] args) {
        Path sessionDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\depth-7-2026-07-19-21-47-v1.7.0");

        Stream<SearchResponse> epdSearchResponseStream = readEpdSearchResponses(sessionDirectory);

        epdSearchResponseStream
                .parallel()
                .forEach(epdSearchResponse -> {

                    SearchReportSaver searchReportSaver = new SearchReportSaver(epdSearchResponse.getSessionId(), sessionDirectory);

                    searchReportSaver.accept(epdSearchResponse.getSearchId(), epdSearchResponse.getEpdSearchResults());

                });

        log.info("Work completed");
    }

    private static Stream<SearchResponse> readEpdSearchResponses(Path sessionDirectory) {
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
                        return (SearchResponse) ois.readObject();
                    } catch (Exception e) {
                        log.error("Failed to deserialize file: " + file, e);
                        return null;
                    }
                }).filter(Objects::nonNull);
    }
}
