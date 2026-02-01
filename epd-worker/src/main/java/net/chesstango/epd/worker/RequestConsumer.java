package net.chesstango.epd.worker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.chesstango.epd.worker.EpdSearchRequest.EPD_REQUESTS_QUEUE_NAME;

/**
 * @author Mauricio Coria
 */
@Slf4j
class RequestConsumer {

    private final Channel channel;

    public RequestConsumer(Channel channel) {
        this.channel = channel;
    }


    public EpdSearchRequest readMessage() throws IOException {
        do {
            GetResponse response = channel.basicGet(EPD_REQUESTS_QUEUE_NAME, true);
            if (response != null) {
                return EpdSearchRequest.decodeRequest(response.getBody());
            } else {
                try {
                    log.info("Waiting for EpdSearchRequest");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Interrupted while waiting for message", e);
                    throw new RuntimeException(e);
                }
            }
        } while (true);
    }
}
