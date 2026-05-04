package net.chesstango.epd.worker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class SearchWorkerMain implements Runnable {

    public static void main(String[] args) throws Exception {
        String rabbitHost = System.getenv("RABBIT_HOST");

        new SearchWorkerMain(rabbitHost).run();
    }

    private final String rabbitHost;

    public SearchWorkerMain(String rabbitHost) {
        if (rabbitHost == null) {
            throw new IllegalArgumentException("rabbitHost and enginesCatalog must be provided");
        }
        this.rabbitHost = rabbitHost;
    }

    @Override
    public void run() {
        log.info("To exit press CTRL+C");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setUsername("guest");
        factory.setPassword("guest");

        log.info("Connecting to RabbitMQ");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();) {

            channel.basicQos(1);

            log.info("Connected to RabbitMQ");

            RequestConsumer requestConsumer = new RequestConsumer(channel);

            ResponseProducer responseProducer = new ResponseProducer(channel);

            SearchWorker searchWorker = new SearchWorker();

            log.info("Waiting for MatchRequest");

            do {
                SearchRequest request = requestConsumer.readMessage();
                log.info("[{}] Received EpdSearchRequest: {}", request.getSessionId(), request.getSearchId());
                SearchResponse response = searchWorker.apply(request);
                responseProducer.publish(response);
            } while (true);

        } catch (IOException | TimeoutException e) {
            log.error("Error", e);
        }

        log.info("Done");
    }
}
