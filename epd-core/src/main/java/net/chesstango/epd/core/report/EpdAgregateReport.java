package net.chesstango.epd.core.report;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.engine.Tango;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
import net.chesstango.reports.search.board.BoardReport;
import net.chesstango.reports.search.evaluation.EvaluationReport;
import net.chesstango.reports.search.nodes.visited.NodesVisitedReport;
import net.chesstango.reports.search.pv.PrincipalVariationReport;
import net.chesstango.reports.search.transposition.TranspositionReport;

import java.io.PrintStream;
import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class EpdAgregateReport implements Report {

    private EpdAgregateModel epdAgregateModel;

    @Override
    public EpdAgregateReport printReport(PrintStream out) {

        out.printf("Version: %s\n", Tango.ENGINE_VERSION);

        new EpdSearchReport()
                .setReportModel(epdAgregateModel.epdSearchModel())
                .printReport(out);

        new BoardReport()
                .setReportModel(epdAgregateModel.boardModel())
                .printReport(out);

        new NodesVisitedReport()
                .setReportModel(epdAgregateModel.nodesVisitedModel())
                .withCutoffStatistics()
                .withNodesVisitedStatistics()
                .printReport(out);

        new PrincipalVariationReport()
                .setReportModel(epdAgregateModel.principalVariationReportModel())
                .printReport(out);

        new EvaluationReport()
                .setReportModel(epdAgregateModel.evaluationReportModel())
                //.withExportEvaluations()
                .withEvaluationsStatistics()
                .printReport(out);

        new TranspositionReport()
                .setTranspositionModel(epdAgregateModel.transpositionModel())
                .printReport(out);

        return this;
    }

    public EpdAgregateReport withEpdSearchResults(String suiteName, List<EpdSearchResult> epdSearchResults) {
        this.epdAgregateModel = EpdAgregateModel.load(suiteName, epdSearchResults);
        return this;
    }

}
