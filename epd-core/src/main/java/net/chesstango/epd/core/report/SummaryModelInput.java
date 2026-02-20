package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record SummaryModelInput(List<EpdSearchResult> epdSearchResults,
                                EpdSearchModel epdSearchModel,
                                NodesModel nodesReportModel,
                                EvaluationModel evaluationReportModel,
                                PrincipalVariationModel principalVariationReportModel,
                                TranspositionModel transpositionModel) {
    public static SummaryModelInput load(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchModel epdSearchModel = new EpdSearchModel().collectStatistics(suiteName, epdSearchResults);
        NodesModel nodesReportModel = new NodesModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        EvaluationModel evaluationReportModel = new EvaluationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationModel principalVariationReportModel = new PrincipalVariationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        TranspositionModel transpositionReportModel = new TranspositionModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        return new SummaryModelInput(epdSearchResults, epdSearchModel, nodesReportModel, evaluationReportModel, principalVariationReportModel, transpositionReportModel);
    }
}
