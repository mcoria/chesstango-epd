package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;

import java.io.*;
import java.util.List;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
public class EpdSearchResponse implements Serializable {
    public final static String EPD_RESPONSES_QUEUE_NAME = "epd_responses";

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String searchId;

    private List<EpdSearchResult> epdSearchResults;

    public static EpdSearchResponse decodeResponse(byte[] request) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(request);
             ObjectInputStream ois = new ObjectInputStream(bis);) {
            return (EpdSearchResponse) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encodeResponse() {
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
