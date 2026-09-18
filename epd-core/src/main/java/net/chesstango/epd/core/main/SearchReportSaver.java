package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.*;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;
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

    public SearchReportSaver(String sessionId, Path directory) {
        this.sessionId = sessionId;
        this.directory = directory;
    }

    @Override
    public void accept(String suiteName, List<EpdSearchResult> epdSearchResults) {
        try {
            EpdAgregateModel epdAgregateModel = EpdAgregateModel.load(sessionId, epdSearchResults);

            CompletableFuture<Void> saveReport = CompletableFuture.supplyAsync(() -> {
                saveAgregateReport(suiteName, epdAgregateModel);
                return null;
            });

            CompletableFuture<Void> saveJson = CompletableFuture.supplyAsync(() -> {
                SummaryModel summaryModel = new SummaryModel().collectStatistics(sessionId, epdAgregateModel);
                saveSummaryJson(suiteName, summaryModel);
                return null;
            });

            CompletableFuture<Void> epdRebase = CompletableFuture.supplyAsync(() -> {
                EpdRebaseModel epdRebaseModel = new EpdRebaseModel().collectStatistics(sessionId, epdSearchResults);
                saveEpdRebase(suiteName, epdRebaseModel);
                return null;
            });

            CompletableFuture<Void> combinedSave = CompletableFuture.allOf(saveReport, saveJson, epdRebase);

            log.info("Saving reports {}", suiteName);

            combinedSave.join();
        } catch (RuntimeException exception) {
            log.error("Error searching: {}", suiteName, exception);
        }
    }


    void saveAgregateReport(String suiteName, EpdAgregateModel epdAgregateModel) {
        Report report = new EpdAgregateReport()
                .setEpdAgregateModel(epdAgregateModel);

        ReportToFile reportToFile = new ReportToFile(directory)
                .save(String.format("%s-report.txt", suiteName), report);
    }

    void saveSummaryJson(String suiteName, SummaryModel summaryModel) {
        Report report = new SummaryReport()
                .setReportModel(summaryModel);

        ReportToFile reportToFile = new ReportToFile(directory)
                .save(String.format("%s.json", suiteName), report);
    }

    void saveEpdRebase(String suiteName, EpdRebaseModel epdRebaseModel) {
        Report report = new EpdRebaseReport()
                .setEpdRebaseModel(epdRebaseModel);

        ReportToFile reportToFile = new ReportToFile(directory)
                .save(String.format("%s", suiteName), report);
    }

}
