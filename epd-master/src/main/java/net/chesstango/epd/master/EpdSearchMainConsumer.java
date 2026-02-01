package net.chesstango.epd.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.worker.EpdSearchResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.chesstango.epd.master.Common.createSessionDirectory;


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

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setSharedExecutor(executorService);

            log.info("Connecting to RabbitMQ");
            try (EpdSearchConsumer epdSearchConsumer = new EpdSearchConsumer(factory)) {

                log.info("Connected to RabbitMQ");

                epdSearchConsumer.setupQueueConsumer(this::accept);

                log.info("Waiting for EpdSearchRequest");

                Thread.sleep(Long.MAX_VALUE);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Done");
    }


    public synchronized void accept(EpdSearchResponse epdSearchResponse) {
        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, epdSearchResponse.getSessionId());

        log.info("Saving EpdSearchResponse for {}", epdSearchResponse.getSessionId());

        String filename = String.format("epdSearchResponse_%s.ser", epdSearchResponse.getSearchId());

        Path filePath = sessionDirectory.resolve(filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(epdSearchResponse);
            log.info("Response serialized to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to serialize response", e);
            throw new RuntimeException(e);
        }
    }
}
