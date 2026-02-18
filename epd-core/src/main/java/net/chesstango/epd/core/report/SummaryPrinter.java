package net.chesstango.epd.core.report;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.chesstango.reports.Printer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Mauricio Coria
 */
public class SummaryPrinter implements Printer {

    private SummaryModel reportModel;
    private PrintStream out;

    @Override
    public SummaryPrinter print() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, reportModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public SummaryPrinter setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    public SummaryPrinter withSearchSummaryModel(SummaryModel summaryModel) {
        this.reportModel = summaryModel;
        return this;
    }
}
