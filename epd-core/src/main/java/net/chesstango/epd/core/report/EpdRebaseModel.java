package net.chesstango.epd.core.report;

import lombok.Getter;
import net.chesstango.board.moves.Move;
import net.chesstango.board.moves.MovePromotion;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.move.SANEncoder;
import net.chesstango.reports.Model;
import net.chesstango.search.SearchResult;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdRebaseModel implements Model<List<EpdSearchResult>> {


    @Getter
    private List<EPD> epdList;

    @Override
    public EpdRebaseModel collectStatistics(String searchGroupName, List<EpdSearchResult> input) {
        epdList = input.stream()
                .map(this::convert)
                .toList();
        return this;
    }


    EPD convert(EpdSearchResult epdSearchResult) {
        SANEncoder sanEncoder = new SANEncoder();

        EPD epd = epdSearchResult.getEpd();
        SearchResult searchResult = epdSearchResult.getSearchResult();

        EPD newEpd = new EPD();
        newEpd.setId(epd.getId());
        newEpd.setPiecePlacement(epd.getPiecePlacement());
        newEpd.setActiveColor(epd.getActiveColor());
        newEpd.setCastingsAllowed(epd.getCastingsAllowed());
        newEpd.setEnPassantSquare(epd.getEnPassantSquare());
        newEpd.setHalfMoveClock(epd.getHalfMoveClock());
        newEpd.setFullMoveClock(epd.getFullMoveClock());

        Move bestMove = searchResult.getBestMove();
        String currentMoveStr = sanEncoder.encodeAlgebraicNotation(toMove(bestMove), epd.toFEN());

        newEpd.setSuppliedMoveStr(currentMoveStr);
        newEpd.setCentiPawnEvaluation(searchResult.getBestEvaluation().toString());

        return newEpd;
    }

    private net.chesstango.gardel.move.Move toMove(Move playedMove) {
        net.chesstango.gardel.move.Move.Square from = net.chesstango.gardel.move.Move.Square.of(playedMove.getFrom().square().getFile(), playedMove.getFrom().square().getRank());
        net.chesstango.gardel.move.Move.Square to = net.chesstango.gardel.move.Move.Square.of(playedMove.getTo().square().getFile(), playedMove.getTo().square().getRank());

        if (playedMove instanceof MovePromotion movePromotion) {
            return net.chesstango.gardel.move.Move.of(from, to, switch (movePromotion.getPromotion()) {
                case KNIGHT_WHITE, KNIGHT_BLACK -> net.chesstango.gardel.move.Move.PromotionPiece.KNIGHT;
                case BISHOP_WHITE, BISHOP_BLACK -> net.chesstango.gardel.move.Move.PromotionPiece.BISHOP;
                case ROOK_WHITE, ROOK_BLACK -> net.chesstango.gardel.move.Move.PromotionPiece.ROOK;
                case QUEEN_WHITE, QUEEN_BLACK -> net.chesstango.gardel.move.Move.PromotionPiece.QUEEN;
                default -> throw new RuntimeException("Invalid promotion " + movePromotion);
            });
        } else {
            return net.chesstango.gardel.move.Move.of(from, to);
        }
    }
}
