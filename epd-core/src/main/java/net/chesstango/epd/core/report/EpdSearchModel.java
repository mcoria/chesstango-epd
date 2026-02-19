package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchModel {
    public String reportTitle;

    public int searches;
    public int success;
    public int successRate;
    public int depthAccuracyAvgPercentageTotal;
    public List<String> failedEntries;

    public long duration;

    public static EpdSearchModel collectStatistics(String reportTitle, List<EpdSearchResult> epdEntries) {
        EpdSearchModel reportModel = new EpdSearchModel();

        List<SearchResult> searchResults = epdEntries.stream().map(EpdSearchResult::getSearchResult).toList();

        reportModel.reportTitle = reportTitle;

        reportModel.searches = epdEntries.size();

        reportModel.success = (int) epdEntries.stream().filter(EpdSearchResult::isSearchSuccess).count();

        reportModel.depthAccuracyAvgPercentageTotal = (int) epdEntries.stream().mapToInt(EpdSearchResult::getDepthAccuracyPct).average().orElse(0);

        reportModel.successRate = ((100 * reportModel.success) / reportModel.searches);

        reportModel.duration = searchResults.stream().mapToLong(SearchResult::getTimeSearching).sum();

        reportModel.failedEntries = new ArrayList<>();

        epdEntries.stream()
                .filter(edpEntry -> !edpEntry.isSearchSuccess())
                .forEach(edpEntry ->
                        reportModel.failedEntries.add(
                                String.format("Fail [%s] - best move found %s",
                                        edpEntry.getText(),
                                        edpEntry.getBestMoveFound()
                                )
                        ));

        return reportModel;
    }
}
