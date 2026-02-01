package net.chesstango.epd.core.search;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
@Accessors(chain = true)
@Getter
@Setter
public class EpdSearchResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final EPD epd;

    private final SearchResult searchResult;

    private boolean isSearchSuccess;

    private String bestMoveFound;

    // Exactitud: de la lista de movimientos en profundidad, que movimientos son exitosos
    private int depthAccuracyPct;

    public EpdSearchResult(EPD epd, SearchResult searchResult) {
        this.epd = epd;
        this.searchResult = searchResult;
    }

    public String getText() {
        return epd.getText();
    }

    public int getBottomMoveCounter() {
        return searchResult.getBottomMoveCounter();
    }
}
