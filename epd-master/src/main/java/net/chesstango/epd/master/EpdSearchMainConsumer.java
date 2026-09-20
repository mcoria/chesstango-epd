package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.main.Common;
import net.chesstango.epd.core.main.SearchReportSaver;
import net.chesstango.epd.core.search.EpdSearchResultCollection;
import net.chesstango.epd.worker.SearchResponse;
import org.apache.commons.cli.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMainConsumer implements Runnable {

    /**
     * Parametros
     * -h RabbitMQ host
     * -i Directorio donde se almacenan los resultados
     * <p>
     * Ejemplo:
     * -h localhost -i C:\java\projects\chess\chess-utils\testing\EPD\database
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CommandLine parsedArgs = parseArguments(args);

        String rabbitHost = parsedArgs.getOptionValue('h');

        String directory = parsedArgs.getOptionValue('i');

        boolean baseline = parsedArgs.hasOption('b');

        log.info("directory={}", directory);

        Path suiteDirectory = Path.of(directory);
        if (!Files.exists(suiteDirectory) || !Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        new EpdSearchMainConsumer(rabbitHost, suiteDirectory, baseline).run();
    }

    private final String rabbitHost;
    private final Path suiteDirectory;
    private final boolean baseline;

    public EpdSearchMainConsumer(String rabbitHost, Path suiteDirectory, boolean baseline) {
        if (rabbitHost == null) {
            throw new IllegalArgumentException("rabbitHost and enginesCatalog must be provided");
        }
        this.rabbitHost = rabbitHost;
        this.suiteDirectory = suiteDirectory;
        this.baseline = baseline;
    }

    @Override
    public void run() {
        log.info("To exit press CTRL+C");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setSharedExecutor(ForkJoinPool.commonPool());

        log.info("Connecting to RabbitMQ");
        try (EpdSearchConsumer epdSearchConsumer = new EpdSearchConsumer(factory)) {

            log.info("Connected to RabbitMQ");

            epdSearchConsumer.setupQueueConsumer(this::accept);

            log.info("Waiting for EpdSearchRequest");

            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Done");
    }


    public void accept(SearchResponse searchResponse) {
        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, searchResponse.getSessionId());

        // Task 1
        CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
            saveResponse(sessionDirectory, searchResponse);
        });

        // Task 2
        CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
            saveReport(sessionDirectory, searchResponse);
        });

        // Wait for both to finish (join blocks the main thread)
        CompletableFuture.allOf(task1, task2).join();
    }

    private void saveReport(Path sessionDirectory, SearchResponse searchResponse) {
        SearchReportSaver searchReportSaver = new SearchReportSaver(searchResponse.getSessionId(), sessionDirectory, baseline);

        searchReportSaver.accept(new EpdSearchResultCollection(searchResponse.getSearchId(), searchResponse.getEpdSearchResults()));
    }

    private void saveResponse(Path sessionDirectory, SearchResponse searchResponse) {
        String filename = String.format("epdSearchResponse_%s.ser", searchResponse.getSearchId());

        Path filePath = sessionDirectory.resolve(filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            log.info("Saving {} {}", searchResponse.getSessionId(), searchResponse.getSearchId());
            oos.writeObject(searchResponse);
            log.info("Response serialized to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to serialize response", e);
            throw new RuntimeException(e);
        }
    }

    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();

        Option rabbitHostOpt = Option.builder("h").argName("rabbitHost").hasArg().required().desc("RabbitMQ host").build();
        options.addOption(rabbitHostOpt);

        Option directoryOpt = Option.builder("i").argName("directory").hasArg().required().desc("directory where results are stored").build();
        options.addOption(directoryOpt);

        Option baselineOpt = Option.builder("b").argName("baseline").desc("baseline search").build();
        options.addOption(baselineOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            log.error("Parsing failed.  Reason: {}", exp.getMessage());
            new HelpFormatter().printHelp(EpdSearchMainConsumer.class.getName(), options);
            System.exit(-1);
        }
        return null;
    }
}
