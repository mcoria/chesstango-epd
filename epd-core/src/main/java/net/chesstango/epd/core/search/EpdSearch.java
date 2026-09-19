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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
        CompletableFuture<Void> stopTask = getStopTask(search);

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

    private CompletableFuture<Void> getStopTask(Search search) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        search.accept(new SetSearchByDepthListenerVisitor(_ -> countDownLatch.countDown()));

        Executor delayed = CompletableFuture.delayedExecutor(timeOut + 1, TimeUnit.SECONDS);

        return CompletableFuture.runAsync(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            search.stopSearch();
        }, delayed);
    }

}
