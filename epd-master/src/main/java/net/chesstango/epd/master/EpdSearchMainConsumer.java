package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.main.Common;
import net.chesstango.epd.core.main.SearchReportSaver;
import net.chesstango.epd.worker.SearchResponse;

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

    public static void main(String[] args) throws Exception {
        String rabbitHost = args[0];

        String directory = args[1];

        System.out.printf("directory={%s}\n", directory);

        Path suiteDirectory = Path.of(directory);
        if (!Files.exists(suiteDirectory) || !Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        new EpdSearchMainConsumer(rabbitHost, suiteDirectory).run();
    }

    private final String rabbitHost;
    private final Path suiteDirectory;

    public EpdSearchMainConsumer(String rabbitHost, Path suiteDirectory) {
        if (rabbitHost == null) {
            throw new IllegalArgumentException("rabbitHost and enginesCatalog must be provided");
        }
        this.rabbitHost = rabbitHost;
        this.suiteDirectory = suiteDirectory;
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
        SearchReportSaver searchReportSaver = new SearchReportSaver(searchResponse.getSessionId(), sessionDirectory);
        searchReportSaver.accept(searchResponse.getSearchId(), searchResponse.getEpdSearchResults());
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
}
