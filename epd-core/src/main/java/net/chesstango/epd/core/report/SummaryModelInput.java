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
}
