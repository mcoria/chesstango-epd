package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchModel implements Model<List<EpdSearchResult>> {
    public String reportTitle;

    public int searches;
    public int success;
    public int successRate;
    public int depthAccuracyAvgPercentageTotal;
    public List<String> failedEntries;

    public long duration;


    @Override
    public EpdSearchModel collectStatistics(String reportTitle, List<EpdSearchResult> epdEntries) {
        List<SearchResult> searchResults = epdEntries.stream().map(EpdSearchResult::getSearchResult).toList();

        this.reportTitle = reportTitle;

        this.searches = epdEntries.size();

        this.success = (int) epdEntries.stream().filter(EpdSearchResult::isSearchSuccess).count();

        this.depthAccuracyAvgPercentageTotal = (int) epdEntries.stream().mapToInt(EpdSearchResult::getDepthAccuracyPct).average().orElse(0);

        this.successRate = ((100 * this.success) / this.searches);

        this.duration = searchResults.stream().mapToLong(SearchResult::getTimeSearching).sum();

        this.failedEntries = new ArrayList<>();

        epdEntries.stream()
                .filter(edpEntry -> !edpEntry.isSearchSuccess())
                .forEach(edpEntry ->
                        this.failedEntries.add(
                                String.format("Fail [%s] - best move found %s",
                                        edpEntry.getText(),
                                        edpEntry.getBestMoveFound()
                                )
                        ));

        return this;
    }
}
