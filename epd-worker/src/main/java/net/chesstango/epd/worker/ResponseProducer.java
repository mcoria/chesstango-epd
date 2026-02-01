package net.chesstango.epd.worker;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static net.chesstango.epd.worker.EpdSearchResponse.EPD_RESPONSES_QUEUE_NAME;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class ResponseProducer {
    private final Channel channel;

    public ResponseProducer(Channel channel) throws IOException {
        this.channel = channel;
        channel.queueDeclare(EPD_RESPONSES_QUEUE_NAME, false, false, false, null);
    }

    public void publish(EpdSearchResponse epdSearchResponse) {
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .build();
            byte[] message = epdSearchResponse.encodeResponse();
            channel.basicPublish("", EPD_RESPONSES_QUEUE_NAME, props, message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
