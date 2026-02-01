package net.chesstango.epd.master;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.worker.EpdSearchResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */

@Slf4j
public class EpdSearchConsumer implements AutoCloseable {

    private final Connection connection;

    private final Channel channel;

    private String cTag;


    public EpdSearchConsumer(ConnectionFactory factory) throws IOException, TimeoutException {
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.channel.basicQos(1);
    }

    @Override
    public void close() throws Exception {
        channel.basicCancel(cTag);
        channel.close();
        connection.close();
    }


    public void setupQueueConsumer(Consumer<EpdSearchResponse> epdSearchResponseConsumer) {
        try {
            cTag = channel.basicConsume(EpdSearchResponse.EPD_RESPONSES_QUEUE_NAME, false, (consumerTag, delivery) -> {

                EpdSearchResponse response = EpdSearchResponse.decodeResponse(delivery.getBody());

                epdSearchResponseConsumer.accept(response);

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            }, consumerTag -> {
                log.info("Queue consumer cancelled {}", cTag);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
