package net.chesstango.epd.core.search;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Mauricio Coria
 */

@Slf4j
public class EpdSearchParallel {
    private final int availableCores;
    private final BlockingQueue<Search> searchPool;
    private boolean searchPoolInitialized = false;

    @Setter
    private EpdSearch epdSearch;

    public EpdSearchParallel() {
        this.availableCores = Runtime.getRuntime().availableProcessors();
        this.searchPool = new LinkedBlockingDeque<>(availableCores);
    }

    public synchronized void setSearchSupplier(SearchSupplier searchSupplier) {
        if (!searchPoolInitialized) {
            for (int i = 0; i < availableCores; i++) {
                searchPool.add(searchSupplier.get());
            }
            searchPoolInitialized = true;
        }
    }

    public List<EpdSearchResult> run(List<EPD> edpEntries) {
        List<EpdSearchResult> epdSearchResults =
                new ArrayList<>(
                        edpEntries
                                .stream()
                                .parallel()
                                .map(this::run)
                                .toList()
                );

        if (epdSearchResults.isEmpty()) {
            throw new RuntimeException("No edp entry was processed");
        }

        epdSearchResults.sort(Comparator.comparing(o -> o.epd().getId()));

        return epdSearchResults;
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
