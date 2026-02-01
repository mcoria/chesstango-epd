package net.chesstango.epd.core.report;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Mauricio Coria
 */
public class SummaryPrinter {

    private SummaryModel reportModel;

    public void print(PrintStream out) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, reportModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public SummaryPrinter withSearchSummaryModel(SummaryModel summaryModel) {
        this.reportModel = summaryModel;
        return this;
    }

}
