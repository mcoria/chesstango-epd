package net.chesstango.epd.core.report;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Printer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * A printer implementation that outputs search summary reports in JSON format.
 * <p>
 * This class is responsible for serializing {@link SummaryModel} objects to JSON
 * using Jackson's ObjectMapper with pretty printing. It provides a fluent API
 * for configuring the output stream and the model to be printed.
 * </p>
 * 
 * @author Mauricio Coria
 */
public class SummaryPrinter implements Printer {

    @Setter
    @Accessors(chain = true)
    private SummaryModel reportModel;


    @Setter
    @Accessors(chain = true)
    private PrintStream out;

    /**
     * Prints the summary model to the configured output stream in JSON format.
     * <p>
     * Uses Jackson's ObjectMapper to serialize the {@link SummaryModel} with
     * pretty printing enabled. If an I/O error occurs during writing, it is
     * wrapped in a RuntimeException.
     * </p>
     *
     * @return this printer instance for method chaining
     * @throws RuntimeException if an I/O error occurs during JSON serialization
     */
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
}
