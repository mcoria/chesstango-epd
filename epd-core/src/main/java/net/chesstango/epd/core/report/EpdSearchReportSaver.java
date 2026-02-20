package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.ReportToFile;

import java.nio.file.Path;
import java.util.List;

import static net.chesstango.epd.core.main.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
public class EpdSearchReportSaver {
    private final ReportToFile reportToFile;

    public EpdSearchReportSaver(Path directory) {
        this.reportToFile = new ReportToFile(directory);
    }

    public void saveReports(String suiteName, List<EpdSearchResult> epdSearchResults) {
        SummaryModelInput summaryModelInput = SummaryModelInput.load(suiteName, epdSearchResults);
        SummaryModel reportModel = new SummaryModel().collectStatistics(SESSION_DATE, summaryModelInput);

        reportToFile.save(String.format("%s-report.txt", suiteName), new EpdAgregateReport()
                .setEvaluationModel(summaryModelInput.evaluationReportModel())
                .setEpdSearchModel(summaryModelInput.epdSearchModel())
                .setNodesModel(summaryModelInput.nodesReportModel())
                .setPrincipalVariationModel(summaryModelInput.principalVariationReportModel())
                .setTranspositionReportModel(summaryModelInput.transpositionModel())
        );

        reportToFile.save(String.format("%s.json", suiteName), new SummaryReport()
                .setReportModel(reportModel)
        );

    }
}
