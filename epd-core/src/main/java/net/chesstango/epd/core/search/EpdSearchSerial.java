package net.chesstango.epd.core.search;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.Search;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Slf4j
public class EpdSearchSerial {

    @Setter
    private EpdSearch epdSearch;

    @Setter
    private Search search;

    public List<EpdSearchResult> run(List<EPD> edpEntries) {
        // Resetting search object before using it
        search.reset();

        return edpEntries
                .stream()
                .map(epd -> epdSearch.run(search, epd))
                .toList();
    }
}
