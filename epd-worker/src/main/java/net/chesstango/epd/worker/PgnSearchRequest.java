package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.PgnSearch;
import net.chesstango.epd.core.search.SearchSupplier;
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

    @Override
    public SearchResponse call()  {
        log.info("[{}] Running PGN search={}", sessionId, pgn.toString());

        PgnSearch pgnSearch = new PgnSearch();

        SearchSupplier searchSupplier = new SearchSupplier();

        List<EpdSearchResult> epdSearchResults = pgnSearch.run(searchSupplier, pgn);

        log.info("[{}] Completed PGN search entries={}", sessionId, epdSearchResults.size());

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(sessionId)
                .setSearchId(getSearchId());
    }
}
