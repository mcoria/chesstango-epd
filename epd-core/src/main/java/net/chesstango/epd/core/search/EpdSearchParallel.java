package net.chesstango.epd.core.search;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */

@Slf4j
public class EpdSearchParallel {

    private final EpdSearch epdSearch;
    private final int availableCores;
    private final BlockingQueue<Search> searchPool;

    private boolean searchPoolInitialized = false;

    public EpdSearchParallel(EpdSearch epdSearch) {
        this.epdSearch = epdSearch;
        this.availableCores = Runtime.getRuntime().availableProcessors();
        this.searchPool = new LinkedBlockingDeque<>(availableCores);
    }


    public List<EpdSearchResult> run(Supplier<Search> searchSupplier, Stream<EPD> edpEntries) {
        initSearchPool(searchSupplier);

        List<EpdSearchResult> epdSearchResults = Collections.synchronizedList(new LinkedList<>());

        edpEntries
                .parallel()
                .map(this::run)
                .forEach(epdSearchResults::add);

        if (epdSearchResults.isEmpty()) {
            throw new RuntimeException("No edp entry was processed");
        }

        epdSearchResults.sort(Comparator.comparing(o -> o.epd().getId()));

        return epdSearchResults;
    }

    void initSearchPool(Supplier<Search> searchSupplier) {
        synchronized (this) {
            if (!searchPoolInitialized) {
                for (int i = 0; i < availableCores; i++) {
                    searchPool.add(searchSupplier.get());
                }
                searchPoolInitialized = true;
            }
        }
    }

    EpdSearchResult run(EPD epd) {
        try {
            Search search = searchPool.take();

            // Resetting search object before using it
            search.reset();

            EpdSearchResult epdSearchResult = epdSearch.run(search, epd);

            searchPool.put(search);

            return epdSearchResult;

        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            log.error("Error processing: {}", epd.getText());
            throw e;
        } catch (InterruptedException e) {
            log.error("Thread interrupted while processing: {}", epd.getText());
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}
