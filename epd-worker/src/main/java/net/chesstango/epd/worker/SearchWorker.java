package net.chesstango.epd.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.SearchSupplier;
import net.chesstango.gardel.epd.EPD;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
class SearchWorker implements Function<SearchRequest, SearchResponse> {

    @Override
    public SearchResponse apply(SearchRequest searchRequest) {
        log.info("[{}] Running EPD search entries={}, depth={}, timeOut={}", searchRequest.getSessionId(), searchRequest.getEpdList().size(), searchRequest.getDepth(), searchRequest.getTimeOut());

        EpdSearch epdSearch = new EpdSearch()
                .setDepth(searchRequest.getDepth());

        if (searchRequest.getTimeOut() > 0) {
            epdSearch.setTimeOut(searchRequest.getTimeOut());
        }

        SearchSupplier searchSupplier = new SearchSupplier();

        Stream<EPD> epdStream = searchRequest.getEpdList().stream();

        List<EpdSearchResult> epdSearchResults = epdSearch.run(searchSupplier, epdStream);

        log.info("[{}] Completed EPD search entries={}, depth={}, timeOut={}", searchRequest.getSessionId(), searchRequest.getEpdList().size(), searchRequest.getDepth(), searchRequest.getTimeOut());

        return new SearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(searchRequest.getSessionId())
                .setSearchId(searchRequest.getSearchId());
    }
}
