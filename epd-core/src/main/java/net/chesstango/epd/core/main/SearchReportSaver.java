package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.EpdAgregateModel;
import net.chesstango.epd.core.report.EpdAgregateReport;
import net.chesstango.epd.core.report.SummaryModel;
import net.chesstango.epd.core.report.SummaryReport;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.ReportToFile;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class SearchReportSaver implements BiConsumer<String, List<EpdSearchResult>> {

    private final String sessionId;
    private final Path directory;

    private EpdAgregateModel epdAgregateModel;
    private SummaryModel summaryModel;

    public SearchReportSaver(String sessionId, Path directory) {
        this.sessionId = sessionId;
        this.directory = directory;
    }

    @Override
    public void accept(String suiteName, List<EpdSearchResult> epdSearchResults) {
        try {
            loadModel(epdSearchResults);

            CompletableFuture<Void> saveReport = CompletableFuture.supplyAsync(() -> {
                saveAgregateReport(suiteName);
                return null;
            });

            CompletableFuture<Void> saveJson = CompletableFuture.supplyAsync(() -> {
                saveSummaryJson(suiteName);
                return null;
            });

            CompletableFuture<Void> combinedSave = CompletableFuture.allOf(saveReport, saveJson);

            log.info("Saving reports {}", suiteName);

            combinedSave.join();
        } catch (RuntimeException exception) {
            log.error("Error searching: {}", suiteName, exception);
        }
    }

    void loadModel(List<EpdSearchResult> epdSearchResults) {
        this.epdAgregateModel = EpdAgregateModel.load(sessionId, epdSearchResults);
        this.summaryModel = new SummaryModel().collectStatistics(sessionId, epdAgregateModel);
    }

    void saveAgregateReport(String suiteName) {
        ReportToFile reportToFile = new ReportToFile(directory);
        reportToFile.save(String.format("%s-report.txt", suiteName), new EpdAgregateReport()
                .setEpdAgregateModel(epdAgregateModel)
        );
    }

    void saveSummaryJson(String suiteName) {
        ReportToFile reportToFile = new ReportToFile(directory);
        reportToFile.save(String.format("%s.json", suiteName), new SummaryReport()
                .setReportModel(summaryModel)
        );
    }

}
