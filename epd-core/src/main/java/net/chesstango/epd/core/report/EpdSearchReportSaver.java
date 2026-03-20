package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.ReportToFile;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchReportSaver {
    private final Path directory;

    private EpdAgregateModel epdAgregateModel;
    private SummaryModel summaryModel;


    public EpdSearchReportSaver(Path directory) {
        this.directory = directory;
    }

    public void loadModel(String sessionId, List<EpdSearchResult> epdSearchResults) {
        this.epdAgregateModel = EpdAgregateModel.load(sessionId, epdSearchResults);
        this.summaryModel = new SummaryModel().collectStatistics(sessionId, epdAgregateModel);
    }

    public void saveReport(String suiteName) {
        ReportToFile reportToFile = new ReportToFile(directory);
        reportToFile.save(String.format("%s-report.txt", suiteName), new EpdAgregateReport()
                .setEpdAgregateModel(epdAgregateModel)
        );
    }

    public void saveJson(String suiteName) {
        ReportToFile reportToFile = new ReportToFile(directory);
        reportToFile.save(String.format("%s.json", suiteName), new SummaryReport()
                .setReportModel(summaryModel)
        );
    }

}
