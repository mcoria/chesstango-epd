package net.chesstango.epd.core.search;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */

@Slf4j
class EpdSearchParallel {

    private final EpdSearch epdSearch;
    private final int availableCores;
    private final BlockingQueue<Search> searchPool;

    private boolean searchPoolInitialized = false;

    EpdSearchParallel(EpdSearch epdSearch) {
        this.epdSearch = epdSearch;
        this.availableCores = Runtime.getRuntime().availableProcessors();
        this.searchPool = new LinkedBlockingDeque<>(availableCores);
    }


    List<EpdSearchResult> run(Supplier<Search> searchSupplier, Stream<EPD> edpEntries) {
        synchronized (this) {
            if (!searchPoolInitialized) {
                for (int i = 0; i < availableCores; i++) {
                    searchPool.add(searchSupplier.get());
                }
                searchPoolInitialized = true;
            }
        }

        List<SearchJob> activeJobs = Collections.synchronizedList(new LinkedList<>());

        List<EpdSearchResult> epdSearchResults = Collections.synchronizedList(new LinkedList<>());

        try (ExecutorService executorService = Executors.newFixedThreadPool(availableCores)) {
            edpEntries.forEach(epd -> executorService.submit(() -> run(epd, activeJobs, epdSearchResults)));

            try {
                if (epdSearch.getTimeOut() != null) {
                    while (!activeJobs.isEmpty()) {
                        Thread.sleep(500);
                        activeJobs.forEach(searchJob -> {
                            if (searchJob.elapsedMillis() >= epdSearch.getTimeOut()) {
                                throw new RuntimeException(String.format("Cambiarme %s", epdSearch.getTimeOut()));
                                //searchJob.search.stopSearching();
                            }
                        });
                    }
                }
            } catch (InterruptedException e) {
                log.error("Stopping executorService....");
                executorService.shutdownNow();
            }
        }

        if (epdSearchResults.isEmpty()) {
            throw new RuntimeException("No edp entry was processed");
        }

        epdSearchResults.sort(Comparator.comparing(o -> o.getEpd().getId()));

        return epdSearchResults;
    }

    void run(EPD epd, List<SearchJob> activeJobs, List<EpdSearchResult> epdSearchResults) {
        SearchJob searchJob = null;
        try {

            Search search = searchPool.take();

            searchJob = new SearchJob(Instant.now(), search);

            activeJobs.add(searchJob);

            // Resetting search object before using it
            search.reset();

            EpdSearchResult epdSearchResult = epdSearch.run(search, epd);

            epdSearchResults.add(epdSearchResult);

            searchPool.put(searchJob.search);

        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            log.error("Error processing: {}", epd.getText());
            throw e;
        } catch (InterruptedException e) {
            log.error("Thread interrupted while processing: {}", epd.getText());
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        } finally {
            assert searchJob != null;

            activeJobs.remove(searchJob);
        }
    }


    record SearchJob(Instant startInstant, Search search) {
        public long elapsedMillis() {
            return Duration.between(startInstant, Instant.now()).toMillis();
        }
    }

}
