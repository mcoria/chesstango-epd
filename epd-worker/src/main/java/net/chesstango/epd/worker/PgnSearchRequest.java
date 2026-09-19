package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.PgnSearch;
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

    private transient PgnSearch pgnSearch;

    @Override
    public void accept(WorkerContext workerContext) {
        pgnSearch = workerContext.getPgnSearch();
    }

    @Override
    public SearchResponse call() {
        log.info("[{}] Running PGN search={}", sessionId, pgn.toString());

        List<EpdSearchResult> epdSearchResults = pgnSearch.run(pgn);

        log.info("[{}] Completed PGN search entries={}", sessionId, epdSearchResults.size());

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(sessionId)
                .setSearchId(getSearchId());
    }
}
