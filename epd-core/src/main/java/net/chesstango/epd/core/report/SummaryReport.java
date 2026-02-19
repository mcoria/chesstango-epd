package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
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
        new SummaryPrinter()
                .setReportModel(reportModel)
                .setOut(output)
                .print();
        return this;
    }

    public SummaryReport withEpdSearchResults(List<EpdSearchResult> epdSearchResults,
                                              EpdSearchModel epdSearchModel,
                                              NodesModel nodesReportModel,
                                              EvaluationModel evaluationReportModel,
                                              PrincipalVariationModel principalVariationReportModel,
                                              TranspositionModel transpositionModel) {

        reportModel = SummaryModel.collectStatics(SESSION_DATE, epdSearchResults, epdSearchModel, nodesReportModel, evaluationReportModel, principalVariationReportModel, transpositionModel);

        return this;
    }
}
