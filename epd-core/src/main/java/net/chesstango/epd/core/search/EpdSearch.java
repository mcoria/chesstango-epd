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
import net.chesstango.search.visitors.SetSearchByDepthListenerVisitor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Slf4j
public class EpdSearch {

    @Setter
    @Getter(AccessLevel.PACKAGE)
    private int depth;

    @Setter
    @Getter(AccessLevel.PACKAGE)
    private Integer timeOut;


    public EpdSearchResult run(Search search, EPD epd) {
        return timeOut == null ? runNow(search, epd) : runTimeOut(search, epd);
    }

    EpdSearchResult runTimeOut(Search search, EPD epd) {
        CompletableFuture<Void> stopTask = stopTask(search);

        EpdSearchResult result = runNow(search, epd);

        stopTask.join();

        return result;
    }

    EpdSearchResult runNow(Search search, EPD epd) {
        Game game = Game.from(epd);

        search.accept(new SetMaxDepthVisitor(depth));

        SearchResult searchResult = search.startSearch(game);

        searchResult.setId(epd.getId());

        return new EpdSearchResult(epd, searchResult);
    }

    private CompletableFuture<Void> stopTask(Search search) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        search.accept(new SetSearchByDepthListenerVisitor(_ -> countDownLatch.countDown()));

        return CompletableFuture.runAsync(() -> {
            try {
                countDownLatch.await();

                Thread.sleep(timeOut * 1000L);

                // Stopping search after depth 1 completes
                search.stopSearch();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
