package net.chesstango.epd.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.epd.core.search.EpdSearchParallel;
import net.chesstango.epd.core.search.EpdSearchSerial;
import net.chesstango.epd.core.search.SearchSupplier;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
public class WorkerContext {
    private final EpdSearchParallel epdSearchParallel;

    private final EpdSearchSerial epdSearchSerial;

    public WorkerContext() {
        SearchSupplier searchSupplier = new SearchSupplier();

        epdSearchParallel = new EpdSearchParallel();
        epdSearchParallel.setSearchSupplier(searchSupplier);

        epdSearchSerial = new EpdSearchSerial();
        epdSearchSerial.setSearch(searchSupplier.get());
    }
}
