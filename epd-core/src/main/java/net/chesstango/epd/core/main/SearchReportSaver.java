package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.report.*;
import net.chesstango.epd.core.report.interpret.EpdSearchResultCompare;
import net.chesstango.epd.core.report.interpret.EpdSearchResultBaseline;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.EpdSearchResultCollection;
import net.chesstango.reports.Report;
import net.chesstango.reports.ReportToFile;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class SearchReportSaver implements Consumer<EpdSearchResultCollection> {

    private final String sessionId;
    private final Path directory;
    private final boolean baseline;

    public SearchReportSaver(String sessionId, Path directory, boolean baseline) {
        this.sessionId = sessionId;
        this.directory = directory;
        this.baseline = baseline;
    }

    @Override
    public void accept(EpdSearchResultCollection epdSearchResultCollection) {
        String suiteName = epdSearchResultCollection.suiteName();
        List<EpdSearchResult> epdSearchResults = epdSearchResultCollection.epdSearchResults();
        try {

            EpdAgregateModel epdAgregateModel = EpdAgregateModel.load(sessionId, epdSearchResults, baseline);

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
