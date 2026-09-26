package net.chesstango.epd.core.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Game;
import net.chesstango.engine.Tango;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;
import net.chesstango.search.SearchResult;
import net.chesstango.search.visitors.SetMaxDepthVisitor;
import net.chesstango.search.visitors.SetSearchByDepthListenerVisitor;

import java.util.concurrent.*;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Slf4j
public class EpdSearch implements AutoCloseable {

    @Getter(AccessLevel.PACKAGE)
    private final Integer depth;

    @Getter(AccessLevel.PACKAGE)
    private final Integer timeOut;

    private Executor executor;
    private ExecutorService executorService;


    public static EpdSearch OpenByDepth(int depth) {
        return new EpdSearch(depth, null);
    }

    public static EpdSearch OpenByTimeOut(int timeOut) {
        return new EpdSearch(Tango.INFINITE_DEPTH, timeOut);
    }


    private EpdSearch(Integer depth, Integer timeOut) {
        if (depth == null && timeOut == null) {
            throw new IllegalArgumentException("Depth or timeOut must be set");
        }
        this.depth = depth;
        this.timeOut = timeOut;

        if (timeOut != null) {
            this.executorService = Executors.newSingleThreadExecutor();
            this.executor = CompletableFuture.delayedExecutor(timeOut + 500, TimeUnit.MILLISECONDS, executorService);
        }
    }

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

                // Stopping search after depth 1 completes
                search.stopSearch();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }


    @Override
    public void close() {
        if (executorService != null) {
            this.executorService.close();
        }
    }
}
