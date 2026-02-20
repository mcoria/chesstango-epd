package net.chesstango.epd.core.report;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.engine.Tango;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.evaluation.EvaluationReport;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.reports.search.nodes.NodesReport;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.pv.PrincipalVariationReport;
import net.chesstango.reports.search.transposition.TranspositionModel;
import net.chesstango.reports.search.transposition.TranspositionReport;

import java.io.PrintStream;
import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class EpdAgregateReport implements Report {

    private PrincipalVariationModel principalVariationModel;
    private EvaluationModel evaluationModel;
    private NodesModel nodesModel;
    private EpdSearchModel epdSearchModel;
    private TranspositionModel transpositionReportModel;

    @Override
    public EpdAgregateReport printReport(PrintStream out) {

        out.printf("Version: %s\n", Tango.ENGINE_VERSION);

        new EpdSearchReport()
                .setReportModel(epdSearchModel)
                .printReport(out);

        new NodesReport()
                .setReportModel(nodesModel)
                .withCutoffStatistics()
                .withNodesVisitedStatistics()
                .printReport(out);

        new EvaluationReport()
                .setReportModel(evaluationModel)
                //.withExportEvaluations()
                .withEvaluationsStatistics()
                .printReport(out);

        new PrincipalVariationReport()
                .setReportModel(principalVariationModel)
                .printReport(out);

        new TranspositionReport()
                .setTranspositionModel(transpositionReportModel)
                .printReport(out);

        return this;
    }

    public EpdAgregateReport withEpdSearchResults(String suiteName, List<EpdSearchResult> epdSearchResults) {
        this.epdSearchModel = new EpdSearchModel().collectStatistics(suiteName, epdSearchResults);
        this.nodesModel = new NodesModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        this.evaluationModel = new EvaluationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        this.principalVariationModel = new PrincipalVariationModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        this.transpositionReportModel = new TranspositionModel().collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        return this;
    }

}
