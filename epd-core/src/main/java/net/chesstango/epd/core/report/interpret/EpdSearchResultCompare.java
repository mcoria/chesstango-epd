package net.chesstango.epd.core.report.interpret;

import net.chesstango.board.moves.Move;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
public record EpdSearchResultCompare(EPD epd, SearchResult searchResult) implements Serializable, EpdSearchResultInterpret {

    public static EpdSearchResultInterpret from(EpdSearchResult epdSearchResult) {
        return new EpdSearchResultCompare(epdSearchResult.epd(), epdSearchResult.searchResult());
    }

    @Override
    public String getBestMove() {
        Move bestMove = searchResult.getBestMove();
        return bestMove.coordinateEncoding();
    }

    @Override
    public boolean isMoveSuccess() {
        return epd.isMoveSuccess(getBestMove());
    }

    @Override
    public Integer getBestEvaluation() {
        return searchResult.getBestEvaluation();
    }

    @Override
    public boolean isEvaluationSuccess() {
        return epd.isEvaluationSuccess(getBestEvaluation().toString());
    }
}
