package net.chesstango.epd.master.filters;

import net.chesstango.gardel.epd.EPD;
import net.chesstango.piazzolla.polyglot.PolyglotBook;

import java.util.function.Predicate;


/**
 * @author Mauricio Coria
 */
public class BookFilter implements Predicate<EPD> {

    private final PolyglotBook polyglotBook;

    public BookFilter(PolyglotBook polyglotBook) {
        this.polyglotBook = polyglotBook;
    }

    @Override
    public boolean test(EPD epd) {
        long position = Long.parseUnsignedLong(epd.getId(), 16);

        if (polyglotBook.search(position) != null) {
            return true;
        }

        return false;
    }
}
