package net.chesstango.epd.core.search;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.moves.Move;
import net.chesstango.board.representations.move.TangoMoveSupplier;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.move.SANDecoder;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.search.Search;
import net.chesstango.search.SearchResult;
import net.chesstango.search.visitors.SetMaxDepthVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Slf4j
public class PgnSearch {
    private final EpdSearchResultBuilder epdSearchResultBuilder = new EpdSearchResultBuilder();
    private Color playingColor;
    private int searchFrom;
    private int searchTo;
    private int depth;
    private Game game;

    public List<EpdSearchResult> run(Supplier<Search> searchSupplier, PGN pgn) {

        readParameters(pgn);

        List<EpdSearchResult> epdSearchResults = new LinkedList<>();

        try {
            Search search = searchSupplier.get();


            // Resetting search object before using it
            search.reset();

            FEN fen = pgn.getFen() == null ? FEN.START_POSITION : pgn.getFen();

            this.game = Game.from(fen);

            SANDecoder<Move> sanDecoder = new SANDecoder<>(new TangoMoveSupplier(game));

            pgn.toEPD().forEach(epd -> {
                if (game.getState().getStatus().isInProgress()) {

                    String moveStr = epd.getSuppliedMoveStr();

                    Move move = sanDecoder.decode(moveStr, game.toFEN());

                    if (move != null) {

                        if (playingColor.equals(game.getPosition().getCurrentTurn()) &&
                                searchFrom <= Integer.parseInt(epd.getFullMoveClock()) &&
                                Integer.parseInt(epd.getFullMoveClock()) <= searchTo
                        ) {
                            EpdSearchResult pgnSearchResult = search(search, epd);

                            epdSearchResults.add(pgnSearchResult);
                        }

                        move.executeMove();
                    } else {
                        throw new RuntimeException(String.format("[%s] %s is not in the list of legal moves for %s", pgn.getEvent(), moveStr, game.toFEN().toString()));
                    }
                }
            });


        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            log.error("Error processing: {}", pgn.toString());
            throw e;
        }

        return epdSearchResults;
    }

    void readParameters(PGN pgn) {
        if (pgn.getWhite().contains("Tango")) {
            this.playingColor = Color.WHITE;
        } else if (pgn.getBlack().contains("Tango")) {
            this.playingColor = Color.BLACK;
        } else {
            throw new RuntimeException("Tango not found in white or black");
        }

        String searchRange = pgn.getOtherTags().getOrDefault("SearchRange", "1:1");

        String[] searchArray = searchRange.split(":");

        this.searchFrom = Integer.parseInt(searchArray[0]);

        this.searchTo = Integer.parseInt(searchArray[1]);

        this.depth = Integer.parseInt(pgn.getOtherTags().getOrDefault("SearchDepth", "1"));
    }

    EpdSearchResult search(Search search, EPD epd) {
        search.accept(new SetMaxDepthVisitor(depth));

        SearchResult searchResult = search.startSearch(game);

        searchResult.setId(epd.getId());

        return epdSearchResultBuilder.apply(epd, searchResult);
    }

}
