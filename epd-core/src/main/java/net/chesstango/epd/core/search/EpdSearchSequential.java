package net.chesstango.epd.core.search;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
class EpdSearchSequential {

    final private EpdSearch epdSearch;

    EpdSearchSequential(EpdSearch epdSearch) {
        this.epdSearch = epdSearch;
    }

    List<EpdSearchResult> run(Supplier<Search> searchSupplier, Stream<EPD> edpEntries) {

        List<EpdSearchResult> epdSearchResults = new LinkedList<>();

        Search search = searchSupplier.get();

        edpEntries.forEach(epd -> {
            try {

                EpdSearchResult epdSearchResult = epdSearch.run(search, epd);

                epdSearchResults.add(epdSearchResult);

            } catch (RuntimeException e) {
                e.printStackTrace(System.err);
                log.error("Error processing: {}", epd.getText());
                throw e;
            }
        });


        if (epdSearchResults.isEmpty()) {
            throw new RuntimeException("No edp entry was processed");
        }

        return epdSearchResults;
    }

}
