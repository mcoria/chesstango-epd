package net.chesstango.epd.core.search;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public record EpdSearchResultCollection(String suiteName, List<EpdSearchResult> epdSearchResults) {
}
