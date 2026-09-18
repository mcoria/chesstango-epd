package net.chesstango.epd.core.search;

import net.chesstango.gardel.epd.EPD;
import net.chesstango.search.SearchResult;

import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
public record EpdSearchResult(EPD epd, SearchResult searchResult) implements Serializable {
}
