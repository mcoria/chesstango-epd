package net.chesstango.epd.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.main.SearchReportSaver;
import net.chesstango.epd.core.search.EpdSearchResultCollection;
import net.chesstango.epd.worker.SearchResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class SearchReaderMain {

    public static void main(String[] args) {
        Path baseDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database");

        boolean baseline = false;

        List<String> sessionDirectories = List.of(
                "depth-5-2026-09-17-09-11-v1.11.0-SNAPSHOT"
                //"depth-6-2026-09-14-09-43-v1.10.0-SNAPSHOT",
                //"depth-7-2026-09-14-09-44-v1.10.0-SNAPSHOT"
                //"depth-5-2026-09-10-08-25-v1.10.0-SNAPSHOT",
                //"depth-6-2026-09-10-08-25-v1.10.0-SNAPSHOT",
                //"depth-7-2026-09-10-08-25-v1.10.0-SNAPSHOT"
        );

        sessionDirectories
                .stream()
                .map(baseDirectory::resolve)
                .filter(Files::isDirectory)
                .forEach(sessionDirectory -> {
                    readSerFiles(sessionDirectory)
                            .parallel()
                            .map(SearchReaderMain::readEpdSearchResponse)
                            .filter(Objects::nonNull)
                            .forEach(searchResponse -> {
                                SearchReportSaver searchReportSaver = new SearchReportSaver(searchResponse.getSessionId(), sessionDirectory, false);
                                searchReportSaver.accept(new EpdSearchResultCollection(searchResponse.getSearchId(), searchResponse.getEpdSearchResults()));
                            });
                });

        log.info("Work completed");
    }

    private static Stream<File> readSerFiles(Path sessionDirectory) {
        File directory = sessionDirectory.toFile();

        log.info("Loading EpdSearchResponse from {}", directory.getAbsolutePath());

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ser"));

        log.info("Found {} ", Arrays.toString(files));

        assert files != null;

        return Stream
                .of(files);
    }

    private static SearchResponse readEpdSearchResponse(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            log.info("Deserializing file: {}", file.getName());
            return (SearchResponse) ois.readObject();
        } catch (Exception e) {
            log.error("Failed to deserialize file: " + file, e);
            return null;
        }
    }
}
