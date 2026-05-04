package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.pgn.PGN;

import java.io.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
public abstract class SearchRequest implements Serializable, Supplier<SearchResponse> {
    public final static String EPD_REQUESTS_QUEUE_NAME = "epd_requests";

    protected String sessionId;
    protected String searchId;

    public static SearchRequest decodeRequest(byte[] request) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(request);
             ObjectInputStream ois = new ObjectInputStream(bis);) {
            return (SearchRequest) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encodeRequest() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
