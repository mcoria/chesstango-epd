package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Report;

import java.io.PrintStream;

/**
 * @author Mauricio Coria
 */
public class EpdRebaseReport implements Report {

    @Setter
    @Accessors(chain = true)
    private EpdRebaseModel epdRebaseModel;

    @Override
    public Report printReport(PrintStream out) {
        epdRebaseModel
                .getEpdList()
                .forEach(out::println);
        return this;
    }
}
