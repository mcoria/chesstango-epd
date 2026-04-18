package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.iteration.IterationEvaluationModel;
import net.chesstango.reports.search.nodes.depth.NodesDepthModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;

import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.pv.iteration.PrincipalVariationIterationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record EpdAgregateModel(List<EpdSearchResult> epdSearchResults,
                               EpdSearchModel epdSearchModel,
                               BoardModel boardModel,
                               NodesDepthModel nodesDepthModel,
                               NodesTypesModel nodesTypesModel,
                               IterationEvaluationModel iterationEvaluationModel,
                               PrincipalVariationModel principalVariationReportModel,
                               PrincipalVariationIterationModel principalVariationIterationReportModel,
                               EvaluationModel evaluationReportModel,
                               TranspositionModel transpositionModel) {
    public static EpdAgregateModel load(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchModel epdSearchModel = new EpdSearchModel().collectStatistics(suiteName, epdSearchResults);
        BoardModel boardModel = new BoardModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        NodesDepthModel nodesDepthModel = new NodesDepthModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        NodesTypesModel nodesTypesModel = new NodesTypesModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        IterationEvaluationModel iterationEvaluationModel = new IterationEvaluationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        EvaluationModel evaluationReportModel = new EvaluationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationModel principalVariationReportModel = new PrincipalVariationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationIterationModel principalVariationIterationReportModel = new PrincipalVariationIterationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        TranspositionModel transpositionReportModel = new TranspositionModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        return new EpdAgregateModel(epdSearchResults, epdSearchModel, boardModel, nodesDepthModel, nodesTypesModel, iterationEvaluationModel, principalVariationReportModel, principalVariationIterationReportModel, evaluationReportModel, transpositionReportModel);
    }
}
