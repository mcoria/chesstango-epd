package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.iteration.IterationEvaluationModel;
import net.chesstango.reports.search.nodes.depth.NodesDepthModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;

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
                                              NodesDepthModel nodesDepthModel,
                                              NodesTypesModel nodesTypesModel,
                                              IterationEvaluationModel iterationEvaluationModel,
                                              EvaluationModel evaluationReportModel,
                                              PrincipalVariationModel principalVariationReportModel,
                                              PrincipalVariationIterationModel principalVariationIterationReportModel,
                                              TranspositionModel transpositionModel) {

        reportModel = new SummaryModel().collectStatistics(SESSION_DATE, new EpdAgregateModel(epdSearchResults, epdSearchModel, boardModel, nodesDepthModel, nodesTypesModel, iterationEvaluationModel, principalVariationReportModel, principalVariationIterationReportModel, evaluationReportModel, transpositionModel));

        return this;
    }
}
