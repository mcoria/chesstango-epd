package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Report;

import java.io.PrintStream;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchReport implements Report {

    private PrintStream out;

    @Setter
    @Accessors(chain = true)
    private String reportTitle = "EpdSearchReport";

    @Setter
    @Accessors(chain = true)
    private EpdSearchModel reportModel;

    @Override
    public EpdSearchReport printReport(PrintStream output) {
        out = output;
        new EpdSearchPrinter()
                .setReportTitle(reportTitle)
                .setReportModel(reportModel)
                .setOut(out)
                .print();
        return this;
    }

    public EpdSearchReport withEdpEntries(List<EpdSearchResult> edpEntries) {
        this.reportModel = new EpdSearchModel().collectStatistics(reportTitle, edpEntries);
        return this;
    }

}
