package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.SearchSupplier;
import net.chesstango.gardel.epd.EPD;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
@Slf4j
public class EpdSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private int depth;
    private int timeOut;

    private List<EPD> epdList;


    @Override
    public SearchResponse call()  {
        log.info("[{}] Running EPD search entries={}, depth={}, timeOut={}", sessionId, epdList.size(), depth, timeOut);

        EpdSearch epdSearch = new EpdSearch()
                .setDepth(depth);

        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        SearchSupplier searchSupplier = new SearchSupplier();

        Stream<EPD> epdStream = epdList.stream();

        List<EpdSearchResult> epdSearchResults = epdSearch.run(searchSupplier, epdStream);

        log.info("[{}] Completed EPD search entries={}, depth={}, timeOut={}", sessionId, epdList.size(), depth, timeOut);

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(sessionId)
                .setSearchId(getSearchId());
    }
}
