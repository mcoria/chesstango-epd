package net.chesstango.epd.core.report.interpret;

import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

/**
 * @author Mauricio Coria
 */
public interface EpdSearchResultInterpret {
    String getBestMove();

    boolean isMoveSuccess();

    Integer getBestEvaluation();

    boolean isEvaluationSuccess();

    EPD epd();
}
