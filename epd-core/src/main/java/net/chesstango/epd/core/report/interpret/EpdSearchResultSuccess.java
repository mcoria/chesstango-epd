package net.chesstango.epd.core.report.interpret;

import net.chesstango.board.moves.Move;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
public record EpdSearchResultSuccess(EPD epd, SearchResult searchResult) implements Serializable {

    public static EpdSearchResultSuccess from(EpdSearchResult epdSearchResult) {
        return new EpdSearchResultSuccess(epdSearchResult.epd(), epdSearchResult.searchResult());
    }

    public String getBestMove() {
        Move bestMove = searchResult.getBestMove();
        return bestMove.coordinateEncoding();
    }

    public boolean isMoveSuccess() {
        return epd.isMoveSuccess(getBestMove());
    }


    public Integer getBestEvaluation() {
        return searchResult.getBestEvaluation();
    }

    public boolean isEvaluationSuccess() {
        return epd.isEvaluationSuccess(getBestEvaluation().toString());
    }
}
