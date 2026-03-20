package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;
import net.chesstango.reports.search.nodes.visited.NodesVisitedModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record EpdAgregateModel(List<EpdSearchResult> epdSearchResults,
                               EpdSearchModel epdSearchModel,
                               BoardModel boardModel,
                               NodesVisitedModel nodesVisitedModel,
                               NodesTypesModel nodesTypesModel,
                               PrincipalVariationModel principalVariationReportModel,
                               EvaluationModel evaluationReportModel,
                               TranspositionModel transpositionModel) {
    public static EpdAgregateModel load(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchModel epdSearchModel = new EpdSearchModel().collectStatistics(suiteName, epdSearchResults);
        BoardModel boardModel = new BoardModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        NodesVisitedModel nodesVisitedModel = new NodesVisitedModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        NodesTypesModel nodesTypesModel = new NodesTypesModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        EvaluationModel evaluationReportModel = new EvaluationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationModel principalVariationReportModel = new PrincipalVariationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        TranspositionModel transpositionReportModel = new TranspositionModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        return new EpdAgregateModel(epdSearchResults, epdSearchModel, boardModel, nodesVisitedModel, nodesTypesModel, principalVariationReportModel, evaluationReportModel, transpositionReportModel);
    }
}
