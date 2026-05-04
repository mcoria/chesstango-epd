package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.engine.Tango;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class Common {

    public static final String SESSION_DATE = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));

    public static Path createSessionDirectory(Path suiteDirectory, String session) {
        String sessionId = createSessionId(session);
        return createSessionDirectoryImp(suiteDirectory, sessionId);
    }

    public static Path createSessionDirectory(Path suiteDirectory, int depth) {
        String sessionId = createSessionId(depth);
        return createSessionDirectoryImp(suiteDirectory, sessionId);
    }

    static String createSessionId(String session) {
        session = session.replace(".", "-");
        return String.format("%s-%s-%s", session, SESSION_DATE, Tango.ENGINE_VERSION);
    }

    static String createSessionId(int depth) {
        return String.format("depth-%d-%s-%s", depth, SESSION_DATE, Tango.ENGINE_VERSION);
    }

    static Path createSessionDirectoryImp(Path suiteDirectory, String sessionId) {
        Path sessionDirectory = suiteDirectory.resolve(sessionId);

        if (Files.exists(sessionDirectory)) {
            log.warn("Session directory already exists {}", sessionDirectory.getFileName().toString());
            return sessionDirectory;
        }

        try {
            log.info("Creating session directory {}", sessionDirectory.getFileName().toString());
            return Files.createDirectory(sessionDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static List<Path> listEpdFiles(Path suiteDirectory, String filePattern) {
        String finalPattern = filePattern.replace(".", "\\.").replace("*", ".*");
        Predicate<String> matchPredicate = Pattern.compile(finalPattern).asMatchPredicate();
        try (Stream<Path> stream = Files.list(suiteDirectory)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(file -> matchPredicate.test(file.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
