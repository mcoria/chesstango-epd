package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.search.SearchResult;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchModel implements Model<List<EpdSearchResult>> {
    String reportTitle;

    int searches;

    int moveSuccess;
    int moveSuccessPct;

    int evaluationSuccess;
    int evaluationSuccessPct;

    List<String> failedEntries;

    long duration;


    @Override
    public EpdSearchModel collectStatistics(String reportTitle, List<EpdSearchResult> epdSearchResults) {
        List<SearchResult> searchResults = epdSearchResults
                .stream()
                .map(EpdSearchResult::getSearchResult)
                .toList();

        this.reportTitle = reportTitle;

        this.searches = epdSearchResults.size();

        this.moveSuccess = (int) epdSearchResults
                .stream()
                .filter(EpdSearchResult::isMoveSuccess)
                .count();
        this.moveSuccessPct = ((100 * this.moveSuccess) / this.searches);

        this.evaluationSuccess = (int) epdSearchResults
                .stream()
                .filter(EpdSearchResult::isEvaluationSuccess)
                .count();
        this.evaluationSuccessPct = ((100 * this.evaluationSuccess) / this.searches);

        this.duration = searchResults
                .stream()
                .mapToLong(SearchResult::getTimeSearching)
                .sum();

        // No coincide el movimiento ni la evaluacion
        this.failedEntries = epdSearchResults
                .stream()
                .filter(epdSearchResult -> !epdSearchResult.isMoveSuccess() && !epdSearchResult.isEvaluationSuccess())
                .map(epdSearchResult ->
                        String.format("Fail [%s] - best move found %s and evaluation %d",
                                epdSearchResult.getEpd().toString(),
                                epdSearchResult.getBestMove(),
                                epdSearchResult.getBestEvaluation())
                )
                .toList();

        return this;
    }
}
