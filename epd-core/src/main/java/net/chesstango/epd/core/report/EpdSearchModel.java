package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.search.SearchResult;

import java.util.ArrayList;
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
    public EpdSearchModel collectStatistics(String reportTitle, List<EpdSearchResult> epdEntries) {
        List<SearchResult> searchResults = epdEntries.stream().map(EpdSearchResult::getSearchResult).toList();

        this.reportTitle = reportTitle;

        this.searches = epdEntries.size();

        this.moveSuccess = (int) epdEntries
                .stream()
                .filter(EpdSearchResult::isMoveSuccess)
                .count();
        this.moveSuccessPct = ((100 * this.moveSuccess) / this.searches);

        this.evaluationSuccess = (int) epdEntries
                .stream()
                .filter(EpdSearchResult::isEvaluationSuccess)
                .count();
        this.evaluationSuccessPct = ((100 * this.evaluationSuccess) / this.searches);

        this.duration = searchResults.stream().mapToLong(SearchResult::getTimeSearching).sum();

        this.failedEntries = new ArrayList<>();

        epdEntries.stream()
                .filter(edpEntry -> !edpEntry.isMoveSuccess() || !edpEntry.isEvaluationSuccess())
                .forEach(edpEntry ->
                        this.failedEntries.add(
                                String.format("Fail [%s] - best move found %s",
                                        edpEntry.getEPDText(),
                                        edpEntry.getBestMove()
                                )
                        ));

        return this;
    }
}
