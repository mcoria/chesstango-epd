package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.EpdSearchSerial;
import net.chesstango.epd.core.search.PgnSearchParams;
import net.chesstango.gardel.pgn.PGN;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
@Slf4j
public class PgnSearchRequest extends SearchRequest {
    private PGN pgn;

    private transient EpdSearchSerial epdSearchSerial;

    @Override
    public void accept(WorkerContext workerContext) {
        epdSearchSerial = workerContext.getEpdSearchSerial();
    }

    @Override
    public SearchResponse call() {
        log.info("[{}] Running PGN search={}", sessionId, pgn.toString());

        PgnSearchParams pgnSearchParams = new PgnSearchParams(pgn);

        EpdSearch epdSearch = new EpdSearch();
        epdSearch.setDepth(pgnSearchParams.getDepth());

        epdSearchSerial.setEpdSearch(epdSearch);

        List<EpdSearchResult> epdSearchResults = epdSearchSerial.run(pgnSearchParams.getEPDs());

        log.info("[{}] Completed PGN search entries={}", sessionId, epdSearchResults.size());

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(sessionId)
                .setSearchId(getSearchId());
    }
}
