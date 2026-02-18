package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Report;

import java.io.PrintStream;

/**
 * @author Mauricio Coria
 */
public class SummaryDiffReport implements Report {

    @Setter
    @Accessors(chain = true)
    private SummaryDiffModel reportModel;


    public SummaryDiffReport printReport(PrintStream output) {
        new SummaryDiffPrinter()
                .setReportModel(reportModel)
                .setOut(output)
                .print();
        return this;
    }

    public SummaryDiffReport withSummaryDiffReportModel(SummaryDiffModel reportModel) {
        this.reportModel = reportModel;
        return this;
    }

}
