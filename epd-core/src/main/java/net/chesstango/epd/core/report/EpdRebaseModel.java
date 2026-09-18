package net.chesstango.epd.core.report;

import lombok.Getter;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.reports.Model;
import net.chesstango.search.SearchResult;

import java.util.List;

/**
 * @author Mauricio Coria
 */
public class EpdRebaseModel implements Model<List<EpdSearchResult>> {


    @Getter
    private List<EPD> epdList;

    @Override
    public EpdRebaseModel collectStatistics(String searchGroupName, List<EpdSearchResult> input) {
        epdList = input.stream()
                .map(this::convert)
                .toList();
        return this;
    }


    EPD convert(EpdSearchResult epdSearchResult) {
        EPD epd = epdSearchResult.getEpd();
        SearchResult searchResult = epdSearchResult.getSearchResult();

        EPD newEpd = new EPD();
        newEpd.setId(epd.getId());
        newEpd.setPiecePlacement(epd.getPiecePlacement());
        newEpd.setActiveColor(epd.getActiveColor());
        newEpd.setCastingsAllowed(epd.getCastingsAllowed());
        newEpd.setEnPassantSquare(epd.getEnPassantSquare());
        newEpd.setHalfMoveClock(epd.getHalfMoveClock());
        newEpd.setFullMoveClock(epd.getFullMoveClock());

        newEpd.setSuppliedMoveStr(searchResult.getBestMove().coordinateEncoding());
        newEpd.setCentiPawnEvaluation(searchResult.getBestEvaluation().toString());

        return newEpd;
    }
}
