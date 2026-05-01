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
class EpdSearchWorker implements Function<EpdSearchRequest, EpdSearchResponse> {

    @Override
    public EpdSearchResponse apply(EpdSearchRequest epdSearchRequest) {
        log.info("[{}] Running EPD search entries={}, depth={}, timeOut={}", epdSearchRequest.getSessionId(), epdSearchRequest.getEpdList().size(), epdSearchRequest.getDepth(), epdSearchRequest.getTimeOut());
        EpdSearch epdSearch = new EpdSearch()
                .setDepth(epdSearchRequest.getDepth());

        if (epdSearchRequest.getTimeOut() > 0) {
            epdSearch.setTimeOut(epdSearchRequest.getTimeOut());
        }

        SearchSupplier searchSupplier = new SearchSupplier();

        Stream<EPD> epdStream = epdSearchRequest.getEpdList().stream();

        List<EpdSearchResult> epdSearchResults = epdSearch.run(searchSupplier, epdStream);

        log.info("[{}] Completed EPD search entries={}, depth={}, timeOut={}", epdSearchRequest.getSessionId(), epdSearchRequest.getEpdList().size(), epdSearchRequest.getDepth(), epdSearchRequest.getTimeOut());

        return new EpdSearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(epdSearchRequest.getSessionId())
                .setSearchId(epdSearchRequest.getSearchId());
    }
}
