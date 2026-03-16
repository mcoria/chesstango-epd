package net.chesstango.epd.core.report;

import net.chesstango.reports.Model;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class SummaryDiffModel implements Model<SummaryDiffModelInput> {
    public record SearchSummaryDiff(int durationPercentage,
                                    int evaluationCoincidencePercentage,
                                    int rootNodesPercentage,
                                    int interiorNodesPercentage,
                                    int quiescenceNodesPercentage,
                                    int leafNodesPercentage,
                                    int terminalNodesPercentage,
                                    int loopNodesPercentage,
                                    int egtbNodesPercentage,
                                    int nodesPercentage,
                                    int evaluatedGamesPercentage,
                                    int executedMovesPercentage,
                                    int ttReadsPercentage,
                                    int ttWritesPercentage
    ) {
        static SearchSummaryDiff calculateDiff(SummaryModel baseLineSearchSummary, SummaryModel searchSummary) {
            int durationPercentage = (int) ((searchSummary.duration * 100) / baseLineSearchSummary.duration);
            int rootNodesPercentage = (int) ((searchSummary.rootNodes * 100) / baseLineSearchSummary.rootNodes);
            int interiorNodesPercentage = baseLineSearchSummary.interiorNodes != 0 ? (int) ((searchSummary.interiorNodes * 100) / baseLineSearchSummary.interiorNodes) : 100;
            int quiescenceNodesPercentage = baseLineSearchSummary.quiescenceNodes != 0 ? (int) ((searchSummary.quiescenceNodes * 100) / baseLineSearchSummary.quiescenceNodes) : 100;
            int leafNodesPercentage = baseLineSearchSummary.leafNodes != 0 ? (int) ((searchSummary.leafNodes * 100) / baseLineSearchSummary.leafNodes) : 100;
            int terminalNodesPercentage = baseLineSearchSummary.terminalNodes != 0 ? (int) ((searchSummary.terminalNodes * 100) / baseLineSearchSummary.terminalNodes) : 100;
            int loopNodesPercentage = baseLineSearchSummary.loopNodes != 0 ? (int) ((searchSummary.loopNodes * 100) / baseLineSearchSummary.loopNodes) : 100;
            int egtbNodesPercentage = baseLineSearchSummary.egtbNodes != 0 ? (int) ((searchSummary.egtbNodes * 100) / baseLineSearchSummary.egtbNodes) : 100;
            int nodesPercentage = (int) ((searchSummary.nodes * 100) / baseLineSearchSummary.nodes);
            int evaluatedGamesPercentage = (int) ((searchSummary.evaluationCounterTotal * 100) / baseLineSearchSummary.evaluationCounterTotal);
            int executedMovesPercentage = (int) ((searchSummary.executedMovesTotal * 100) / baseLineSearchSummary.executedMovesTotal);
            int ttReadsPercentage = baseLineSearchSummary.ttReadsTotal != 0 ? (int) ((searchSummary.ttReadsTotal * 100) / baseLineSearchSummary.ttReadsTotal) : 100;
            int ttWritesPercentage = baseLineSearchSummary.ttWritesTotal != 0 ? (int) ((searchSummary.ttWritesTotal * 100) / baseLineSearchSummary.ttWritesTotal) : 100;

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
                    rootNodesPercentage,
                    interiorNodesPercentage,
                    quiescenceNodesPercentage,
                    leafNodesPercentage,
                    terminalNodesPercentage,
                    loopNodesPercentage,
                    egtbNodesPercentage,
                    nodesPercentage,
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


