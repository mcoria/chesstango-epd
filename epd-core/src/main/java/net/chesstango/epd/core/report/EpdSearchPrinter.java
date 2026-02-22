package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Printer;

import java.io.PrintStream;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchPrinter implements Printer {

    @Setter
    @Accessors(chain = true)
    private PrintStream out;

    @Setter
    @Accessors(chain = true)
    private String reportTitle = "EpdSearchReport";

    @Setter
    @Accessors(chain = true)
    private EpdSearchModel reportModel;


    @Override
    public EpdSearchPrinter print() {
        out.printf("--------------------------------------------------------------------------------------------------------------------------------------------------------%n");
        out.printf("EpdSearchReport: %s\n\n", reportModel.reportTitle);

        if (reportModel.failedEntries.isEmpty()) {
            out.println("\tall tests executed successfully !!!!");
        } else {
            for (String failedTest : reportModel.failedEntries) {
                out.printf("\t%s\n", failedTest);
            }
        }

        out.printf("Searches        : %d%n", reportModel.searches);
        out.printf("Success rate    : %d%%%n", reportModel.successRate);
        out.printf("Depth Accuracy  : %d%%%n", reportModel.depthAccuracyAvgPercentageTotal);
        out.printf("Time taken      : %dms%n", reportModel.duration);

        return this;
    }

    public EpdSearchPrinter withEdpEntries(List<EpdSearchResult> edpEntries) {
        this.reportModel = new EpdSearchModel().collectStatistics(reportTitle, edpEntries);
        return this;
    }

}
