package net.chesstango.epd.core.search;

import net.chesstango.board.moves.Move;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;
import net.chesstango.search.SearchResultByDepth;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdSearchResultBuildWithBestMove implements EpdSearchResultBuilder {

    @Override
    public EpdSearchResult apply(EPD epd, SearchResult searchResult) {
        Move bestMove = searchResult.getBestMove();
        String moveCoordinate = bestMove.coordinateEncoding();
        boolean success = epd.isMoveSuccess(moveCoordinate);

        return new EpdSearchResult(epd, searchResult)
                .setBestMoveFound(moveCoordinate)
                .setSearchSuccess(success)
                .setDepthAccuracyPct(calculateAccuracy(epd, searchResult.getSearchResultByDepths()));
    }


    /**
     * Calculates the accuracy percentage of successful moves across all search depths.
     * This method determines what percentage of the best moves found at each depth level
     * match the expected successful moves defined in the EPD position.
     *
     * @param epd the EPD position containing the expected successful moves
     * @param searchResultByDepths list of search results for each depth level
     * @return the accuracy percentage (0-100) representing the ratio of successful moves
     *         to total depth levels searched, or 0 if no search results are available
     */
    private int calculateAccuracy(EPD epd, List<SearchResultByDepth> searchResultByDepths) {
        if (!searchResultByDepths.isEmpty()) {
            long successCounter = searchResultByDepths
                    .stream()
                    .map(SearchResultByDepth::getBestMove)
                    .map(Move::coordinateEncoding)
                    .filter(epd::isMoveSuccess)
                    .count();
            return (int) (successCounter * 100 / searchResultByDepths.size());
        }
        return 0;
    }
}
