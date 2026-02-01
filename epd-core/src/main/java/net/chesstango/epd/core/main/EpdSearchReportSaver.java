package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.engine.Tango;
import net.chesstango.epd.core.report.EpdSearchReport;
import net.chesstango.epd.core.report.EpdSearchReportModel;
import net.chesstango.epd.core.report.SummaryModel;
import net.chesstango.epd.core.report.SummaryPrinter;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.tree.evaluation.EvaluationModel;
import net.chesstango.reports.tree.evaluation.EvaluationReport;
import net.chesstango.reports.tree.nodes.NodesModel;
import net.chesstango.reports.tree.nodes.NodesReport;
import net.chesstango.reports.tree.pv.PrincipalVariationModel;
import net.chesstango.reports.tree.pv.PrincipalVariationReport;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static net.chesstango.epd.core.main.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchReportSaver {

    private final Path sessionDirectory;

    public EpdSearchReportSaver(Path sessionDirectory) {
        this.sessionDirectory = sessionDirectory;
    }

    public void saveReport(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchReportModel epdSearchReportModel = EpdSearchReportModel.collectStatistics(suiteName, epdSearchResults);
        NodesModel nodesReportModel = NodesModel.collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        EvaluationModel evaluationReportModel = EvaluationModel.collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationModel principalVariationReportModel = PrincipalVariationModel.collectStatics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        SummaryModel summaryModel = SummaryModel.collectStatics(SESSION_DATE, epdSearchResults, epdSearchReportModel, nodesReportModel, evaluationReportModel, principalVariationReportModel);

        saveReports(suiteName, epdSearchReportModel, nodesReportModel, evaluationReportModel, principalVariationReportModel);

        saveSearchSummary(suiteName, summaryModel);
    }

    private void saveSearchSummary(String suiteName, SummaryModel summaryModel) {
        Path searchSummaryPath = sessionDirectory.resolve(String.format("%s.json", suiteName));

        try (PrintStream out = new PrintStream(new FileOutputStream(searchSummaryPath.toFile()), true)) {
            new SummaryPrinter()
                    .withSearchSummaryModel(summaryModel)
                    .print(out);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void saveReports(String suiteName, EpdSearchReportModel epdSearchReportModel, NodesModel nodesReportModel, EvaluationModel evaluationReportModel, PrincipalVariationModel principalVariationReportModel) {
        Path suitePathReport = sessionDirectory.resolve(String.format("%s-report.txt", suiteName));

        try (PrintStream out = new PrintStream(new FileOutputStream(suitePathReport.toFile()), true)) {

            printReports(out, epdSearchReportModel, nodesReportModel, evaluationReportModel, principalVariationReportModel);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void printReports(PrintStream output, EpdSearchReportModel epdSearchReportModel, NodesModel nodesReportModel, EvaluationModel evaluationReportModel, PrincipalVariationModel principalVariationReportModel) {
        output.printf("Version: %s\n", Tango.ENGINE_VERSION);

        new EpdSearchReport()
                .setReportModel(epdSearchReportModel)
                .printReport(output);

        new NodesReport()
                .setReportModel(nodesReportModel)
                .withCutoffStatistics()
                .withNodesVisitedStatistics()
                .printReport(output);

        new EvaluationReport()
                .setReportModel(evaluationReportModel)
                //.withExportEvaluations()
                .withEvaluationsStatistics()
                .printReport(output);


        new PrincipalVariationReport()
                .setReportModel(principalVariationReportModel)
                .printReport(output);
    }
}
