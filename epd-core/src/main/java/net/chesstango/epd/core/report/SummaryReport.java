package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evalcache.EvaluationCacheModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.evaluation.iteration.EvaluationIterationModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;
import net.chesstango.reports.search.nodes.visited.VisitedModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.pv.iteration.PrincipalVariationIterationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;

import java.io.PrintStream;
import java.util.List;

import static net.chesstango.epd.core.main.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
public class SummaryReport implements Report {

    @Setter
    @Accessors(chain = true)
    private SummaryModel reportModel;


    public SummaryReport printReport(PrintStream output) {
        new SummaryPrinterJson()
                .setReportModel(reportModel)
                .setOut(output)
                .print();
        return this;
    }

    public SummaryReport withEpdSearchResults(List<EpdSearchResult> epdSearchResults,
                                              EpdSearchModel epdSearchModel,
                                              BoardModel boardModel,
                                              VisitedModel nodesVisitedModel,
                                              NodesTypesModel nodesTypesModel,

                                              EvaluationModel evaluationReportModel,
                                              EvaluationIterationModel evaluationIterationModel,

                                              PrincipalVariationModel principalVariationReportModel,
                                              PrincipalVariationIterationModel principalVariationIterationReportModel,

                                              TranspositionModel transpositionModel,
                                              EvaluationCacheModel evaluationCacheModel) {

        reportModel = new SummaryModel().collectStatistics(SESSION_DATE, new EpdAgregateModel(epdSearchResults, epdSearchModel, boardModel, nodesVisitedModel, nodesTypesModel, principalVariationReportModel, principalVariationIterationReportModel, evaluationReportModel, evaluationIterationModel, transpositionModel, evaluationCacheModel));

        return this;
    }
}
