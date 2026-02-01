package net.chesstango.epd.master.filters;

import net.chesstango.gardel.epd.EPD;

import java.util.function.Predicate;


/**
 * @author Mauricio Coria
 */
public class PlayerFilter implements Predicate<EPD> {

    private final String playerName;

    public PlayerFilter(String playerName) {
        this.playerName = playerName.toLowerCase();
    }

    @Override
    public boolean test(EPD epd) {
        if (epd.getC3() != null) {
            if ("w".equals(epd.getActiveColor()) &&
                    epd.getC3().toLowerCase().contains(playerName) &&
                    "result='1-0'".equals(epd.getC5())) {
                return true;
            }
        }

        if (epd.getC4() != null) {
            if ("b".equals(epd.getActiveColor()) &&
                    epd.getC4().toLowerCase().contains(playerName) &&
                    "result='0-1'".equals(epd.getC5())) {
                return true;
            }
        }

        return false;
    }
}
