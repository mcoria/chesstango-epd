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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */

@Slf4j
class EpdSearchParallel {

    final private EpdSearch epdSearch;

    EpdSearchParallel(EpdSearch epdSearch) {
        this.epdSearch = epdSearch;
    }


    List<EpdSearchResult> run(Supplier<Search> searchSupplier, Stream<EPD> edpEntries) {
        final int availableCores = Runtime.getRuntime().availableProcessors();

        AtomicInteger pendingJobsCounter = new AtomicInteger(0);

        List<SearchJob> activeJobs = Collections.synchronizedList(new LinkedList<>());

        List<EpdSearchResult> epdSearchResults = Collections.synchronizedList(new LinkedList<>());

        BlockingQueue<Search> searchPool = new LinkedBlockingDeque<>(availableCores);
        for (int i = 0; i < availableCores; i++) {
            searchPool.add(searchSupplier.get());
        }

        try (ExecutorService executorService = Executors.newFixedThreadPool(availableCores)) {
            edpEntries.forEach(epd -> {
                pendingJobsCounter.incrementAndGet();
                executorService.submit(() -> {
                    SearchJob searchJob = null;
                    try {
                        Instant startInstant = Instant.now();

                        Search search = searchPool.take();

                        searchJob = new SearchJob(startInstant, search);

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

                        pendingJobsCounter.decrementAndGet();
                    }
                });
            });

            try {
                if (epdSearch.getTimeOut() != null) {
                    while (pendingJobsCounter.get() > 0) {
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

        if (pendingJobsCounter.get() > 0) {
            throw new RuntimeException(String.format("Todavia siguen pendiente %d busquedas", pendingJobsCounter.get()));
        }

        epdSearchResults.sort(Comparator.comparing(o -> o.getEpd().getId()));

        return epdSearchResults;
    }


    record SearchJob(Instant startInstant, Search search) {
        public long elapsedMillis() {
            return Duration.between(startInstant, Instant.now()).toMillis();
        }
    }

}
