package net.chesstango.epd.core.search;

import lombok.Getter;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.moves.Move;
import net.chesstango.board.representations.move.TangoMoveSupplier;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.move.SANDecoder;
import net.chesstango.gardel.pgn.PGN;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class PgnSearchParams {
    private final PGN pgn;
    private final Color playingColor;
    private final int searchFrom;
    private final int searchTo;
    private final Game game;

    @Getter
    private final int depth;


    public PgnSearchParams(PGN pgn) {
        this.pgn = pgn;

        if (pgn.getWhite().contains("Tango")) {
            this.playingColor = Color.WHITE;
        } else if (pgn.getBlack().contains("Tango")) {
            this.playingColor = Color.BLACK;
        } else {
            throw new RuntimeException("Tango not found in white or black");
        }

        String searchRange = pgn.getTag("SearchRange").orElse("1:1");

        String[] searchArray = searchRange.split(":");

        this.searchFrom = Integer.parseInt(searchArray[0]);

        this.searchTo = Integer.parseInt(searchArray[1]);

        this.depth = Integer.parseInt(pgn.getTag("SearchDepth").orElse("1"));

        this.game = Game.from(pgn.getFen() == null ? FEN.START_POSITION : pgn.getFen());
    }

    public List<EPD> getEPDs() {
        List<EPD> epdList = new ArrayList<>();

        SANDecoder<Move> sanDecoder = new SANDecoder<>(new TangoMoveSupplier(game));
        pgn
                .toEPD()
                .forEach(epd -> {
                    if (game.getState().getStatus().isInProgress()) {
                        String suppliedMoveStr = epd.getSuppliedMoveStr();
                        Move move = sanDecoder.decode(suppliedMoveStr, game.toFEN());
                        if (move != null) {
                            if (playingColor.equals(game.getPosition().getCurrentTurn()) &&
                                    searchFrom <= Integer.parseInt(epd.getFullMoveClock()) &&
                                    Integer.parseInt(epd.getFullMoveClock()) <= searchTo) {
                                epdList.add(epd);
                            }
                            move.executeMove();
                        } else {
                            throw new RuntimeException(String.format("[%s] %s is not in the list of legal moves for %s", pgn.getEvent(), suppliedMoveStr, game.toFEN().toString()));
                        }
                    }
                });
        return epdList;
    }
}
