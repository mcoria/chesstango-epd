package net.chesstango.epd.core.report;

import net.chesstango.reports.Model;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class SummaryDiffModel implements Model<SummaryDiffModelInput> {
    public record SearchSummaryDiff(int durationPercentage,
                                    int evaluationCoincidencePercentage,
                                    int visitedNodesPercentage,
                                    int evaluatedGamesPercentage,
                                    int executedMovesPercentage,
                                    int ttReadsPercentage,
                                    int ttWritesPercentage
    ) {
        static SearchSummaryDiff calculateDiff(SummaryModel baseLineSearchSummary, SummaryModel searchSummary) {
            int durationPercentage = (int) ((searchSummary.duration * 100) / baseLineSearchSummary.duration);
            int visitedNodesPercentage = (int) ((searchSummary.visitedNodesTotal * 100) / baseLineSearchSummary.visitedNodesTotal);
            int evaluatedGamesPercentage = (int) ((searchSummary.evaluationCounterTotal * 100) / baseLineSearchSummary.evaluationCounterTotal);
            int executedMovesPercentage = (int) ((searchSummary.executedMovesTotal * 100) / baseLineSearchSummary.executedMovesTotal);
            int ttReadsPercentage = baseLineSearchSummary.ttReadsTotal != 0 ? (int) ((searchSummary.ttReadsTotal * 100) / baseLineSearchSummary.ttReadsTotal) : 0;
            int ttWritesPercentage = baseLineSearchSummary.ttWritesTotal != 0 ? (int) ((searchSummary.ttWritesTotal * 100) / baseLineSearchSummary.ttWritesTotal) : 0;

            int evaluationCoincidences = 0;
            List<SummaryModel.SearchSummaryModeDetail> baseLineSummaryModeDetailListModeDetail = baseLineSearchSummary.searchDetailList;
            List<SummaryModel.SearchSummaryModeDetail> summaryModeDetailListModeDetail = searchSummary.searchDetailList;
            int baseLineSearches = baseLineSummaryModeDetailListModeDetail.size();
            int searches = summaryModeDetailListModeDetail.size();

            for (int i = 0; i < Math.min(baseLineSearches, searches); i++) {
                SummaryModel.SearchSummaryModeDetail baseMoveDetail = baseLineSummaryModeDetailListModeDetail.get(i);
                SummaryModel.SearchSummaryModeDetail moveDetail = summaryModeDetailListModeDetail.get(i);

                if (baseMoveDetail.evaluation == moveDetail.evaluation) {
                    evaluationCoincidences++;
                }
            }

            int evaluationCoincidencePercentage = (evaluationCoincidences * 100) / baseLineSearches;

            return new SearchSummaryDiff(durationPercentage,
                    evaluationCoincidencePercentage,
                    visitedNodesPercentage,
                    evaluatedGamesPercentage,
                    executedMovesPercentage,
                    ttReadsPercentage,
                    ttWritesPercentage);
        }
    }

    record SummaryDiffPair(SummaryModel searchSummary, SearchSummaryDiff searchSummaryDiff) {
    }

    String suiteName;
    int elements;
    SummaryModel baseLineSearchSummary;
    List<SummaryDiffPair> searchSummaryPairs;

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
                .map(searchSummary -> new SummaryDiffPair(searchSummary, SearchSummaryDiff.calculateDiff(baseLineSearchSummary, searchSummary)))
                .toList();

        return reportModel;
    }


}


