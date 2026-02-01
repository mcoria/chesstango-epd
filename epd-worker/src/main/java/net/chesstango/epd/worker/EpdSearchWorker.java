package net.chesstango.epd.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.EpdSearchResultBuildWithBestMove;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.builders.AlphaBetaBuilder;

import java.util.List;
import java.util.function.Function;

/**
 * @author Mauricio Coria
 */
@Slf4j
class EpdSearchWorker implements Function<EpdSearchRequest, EpdSearchResponse> {

    @Override
    public EpdSearchResponse apply(EpdSearchRequest epdSearchRequest) {
        log.info("[{}] Running EPD search entries={}, depth={}, timeOut={}", epdSearchRequest.getSessionId(), epdSearchRequest.getEpdList().size(), epdSearchRequest.getDepth(), epdSearchRequest.getTimeOut());
        EpdSearch epdSearch = new EpdSearch()
                .setSearchSupplier(() -> AlphaBetaBuilder
                        .createDefaultBuilderInstance()
                        // Hasta v0.0.27 se utilizó EvaluatorSEandImp02
                        // (ahora EvaluatorImp04) como evaluador
                        .withGameEvaluator(Evaluator.getInstance())
                        .withStatistics()
                        .build())
                .setDepth(epdSearchRequest.getDepth())
                .setEpdSearchResultBuilder(new EpdSearchResultBuildWithBestMove());

        if (epdSearchRequest.getTimeOut() > 0) {
            epdSearch.setTimeOut(epdSearchRequest.getTimeOut());
        }

        List<EpdSearchResult> epdSearchResults = epdSearch.run(epdSearchRequest.getEpdList());

        log.info("[{}] Completed EPD search entries={}, depth={}, timeOut={}", epdSearchRequest.getSessionId(), epdSearchRequest.getEpdList().size(), epdSearchRequest.getDepth(), epdSearchRequest.getTimeOut());

        return new EpdSearchResponse()
                .setEpdSearchResults(epdSearchResults)
                .setSessionId(epdSearchRequest.getSessionId())
                .setSearchId(epdSearchRequest.getSearchId());
    }
}
