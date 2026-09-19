package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchParallel;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.gardel.epd.EPD;

import java.io.Serial;
import java.util.List;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
@Slf4j
public class EpdSearchRequest extends SearchRequest {
    private int depth;
    private int timeOut;

    private List<EPD> epdList;

    private transient EpdSearchParallel epdSearchParallel;

    @Override
    public void accept(WorkerContext workerContext) {
        epdSearchParallel = workerContext.getEpdSearchParallel();
    }

    @Override
    public SearchResponse call() {
        log.info("[{}] Running EPD search entries={}, depth={}, timeOut={}", sessionId, epdList.size(), depth, timeOut);

        EpdSearch epdSearch = new EpdSearch();
        epdSearch.setDepth(depth);
        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        epdSearchParallel.setEpdSearch(epdSearch);

        List<EpdSearchResult> epdSearchResults = epdSearchParallel.run(epdList);

        log.info("[{}] Completed EPD search entries={}, depth={}, timeOut={}", sessionId, epdList.size(), depth, timeOut);

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(sessionId)
                .setSearchId(getSearchId());
    }

}
