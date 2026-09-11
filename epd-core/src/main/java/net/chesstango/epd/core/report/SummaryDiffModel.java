package net.chesstango.epd.core.report;

import lombok.Getter;
import net.chesstango.reports.Model;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Getter
public class SummaryDiffModel implements Model<SummaryDiffModelInput> {

    private String suiteName;
    private int elements;
    private SummaryModel baseLineSearchSummary;
    private List<SummaryDiffPair> searchSummaryPairs;

    @Override
    public SummaryDiffModel collectStatistics(String suiteName, SummaryDiffModelInput input) {
        SummaryModel baseLineSearchSummary = input.baseLineSearchSummary();
        List<SummaryModel> searchSummaryList = input.searchSummaryList();
        SummaryDiffModel reportModel = new SummaryDiffModel();

        reportModel.suiteName = suiteName;
        reportModel.elements = searchSummaryList.size();
        reportModel.baseLineSearchSummary = baseLineSearchSummary;
        reportModel.searchSummaryPairs = searchSummaryList
                .stream()
                .map(searchSummary -> new SummaryDiffPair(searchSummary, SummaryDiffPercentages.calculateDiff(baseLineSearchSummary, searchSummary)))
                .toList();

        return reportModel;
    }


}


