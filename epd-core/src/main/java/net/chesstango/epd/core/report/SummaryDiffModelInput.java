package net.chesstango.epd.core.report;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record SummaryDiffModelInput(SummaryModel baseLineSearchSummary,
                                    List<SummaryModel> searchSummaryList) {
}
