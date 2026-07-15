package net.chesstango.epd.core.search;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.board.moves.Move;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
public class EpdSearchResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final EPD epd;

    private final SearchResult searchResult;

    public EpdSearchResult(EPD epd, SearchResult searchResult) {
        this.epd = epd;
        this.searchResult = searchResult;
    }

    public int getBottomMoveCounter() {
        return searchResult.getBottomMoveCounter();
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
