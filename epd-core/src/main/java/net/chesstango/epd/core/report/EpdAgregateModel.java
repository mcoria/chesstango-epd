package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evalcache.EvaluationCacheModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.evaluation.iteration.EvaluationIterationModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;
import net.chesstango.reports.search.nodes.visited.VisitedModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.pv.iteration.PrincipalVariationIterationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;
import net.chesstango.search.SearchResult;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record EpdAgregateModel(List<EpdSearchResult> epdSearchResults,
                               EpdSearchModel epdSearchModel,
                               BoardModel boardModel,

                               VisitedModel nodesVisitedModel,
                               NodesTypesModel nodesTypesModel,

                               PrincipalVariationModel principalVariationReportModel,
                               PrincipalVariationIterationModel principalVariationIterationReportModel,

                               EvaluationModel evaluationReportModel,
                               EvaluationIterationModel evaluationIterationModel,

                               TranspositionModel transpositionModel,
                               EvaluationCacheModel evaluationCacheModel) {
    public static EpdAgregateModel load(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchModel epdSearchModel = new EpdSearchModel().collectStatistics(suiteName, epdSearchResults);

        List<SearchResult> searchResults = epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList();

        BoardModel boardModel = new BoardModel().collectStatistics(suiteName, searchResults);
        VisitedModel visitedModel = new VisitedModel().collectStatistics(suiteName, searchResults);
        NodesTypesModel nodesTypesModel = new NodesTypesModel().collectStatistics(suiteName, searchResults);

        EvaluationModel evaluationReportModel = new EvaluationModel().collectStatistics(suiteName, searchResults);
        EvaluationIterationModel iterationEvaluationModel = new EvaluationIterationModel().collectStatistics(suiteName, searchResults);

        PrincipalVariationModel principalVariationReportModel = new PrincipalVariationModel().collectStatistics(suiteName, searchResults);
        PrincipalVariationIterationModel principalVariationIterationReportModel = new PrincipalVariationIterationModel().collectStatistics(suiteName, searchResults);

        TranspositionModel transpositionReportModel = new TranspositionModel().collectStatistics(suiteName, searchResults);

        EvaluationCacheModel evaluationCacheModel = new EvaluationCacheModel().collectStatistics(suiteName, searchResults);

        return new EpdAgregateModel(
                epdSearchResults,
                epdSearchModel,
                boardModel,
                visitedModel, nodesTypesModel,
                principalVariationReportModel, principalVariationIterationReportModel,
                evaluationReportModel, iterationEvaluationModel,
                transpositionReportModel,
                evaluationCacheModel
        );
    }
}
