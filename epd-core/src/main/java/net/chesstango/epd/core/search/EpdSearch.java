package net.chesstango.epd.core.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Game;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;
import net.chesstango.search.SearchResult;
import net.chesstango.search.visitors.SetMaxDepthVisitor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Slf4j
public class EpdSearch {
    private final EpdSearchResultBuilder epdSearchResultBuilder = new EpdSearchResultBuilder();

    @Setter
    @Getter(AccessLevel.PACKAGE)
    private int depth;

    @Setter
    @Getter(AccessLevel.PACKAGE)
    private Integer timeOut;

    public List<EpdSearchResult> run(Supplier<Search> searchSupplier, Stream<EPD> edpEntries) {
        return new EpdSearchParallel(this).run(searchSupplier, edpEntries);
    }

    public EpdSearchResult run(Search search, EPD epd) {
        Game game = Game.from(epd);

        search.accept(new SetMaxDepthVisitor(depth));

        SearchResult searchResult = search.startSearch(game);

        searchResult.setId(epd.getId());

        return epdSearchResultBuilder.apply(epd, searchResult);
    }

}
