package net.chesstango.epd.core.search;

import net.chesstango.board.moves.Move;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;
import net.chesstango.search.SearchResultByDepth;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Mauricio Coria
 */
public class EpdSearchResultBuilder implements BiFunction<EPD, SearchResult, EpdSearchResult> {

    @Override
    public EpdSearchResult apply(EPD epd, SearchResult searchResult) {
        return new EpdSearchResult(epd, searchResult)
                .setDepthAccuracyPct(calculateAccuracy(epd, searchResult));
    }


    /**
     * Calculates the accuracy percentage of successful moves across all search depths.
     * This method determines what percentage of the best moves found at each depth level
     * match the expected successful moves defined in the EPD position.
     *
     * @param epd                  the EPD position containing the expected successful moves
     * @param searchResult         the search result containing the search depths and best moves
     * @return the accuracy percentage (0-100) representing the ratio of successful moves
     * to total depth levels searched, or 0 if no search results are available
     */
    private int calculateAccuracy(EPD epd, SearchResult searchResult) {
        List<SearchResultByDepth> searchResultByDepths = searchResult.getSearchResultByDepths();
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
