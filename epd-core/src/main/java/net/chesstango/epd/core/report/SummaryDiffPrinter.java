package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Printer;
import net.chesstango.reports.PrinterTxtTable;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static net.chesstango.reports.PrinterTxtTable.TextAlignment.LEFT;
import static net.chesstango.reports.PrinterTxtTable.TextAlignment.RIGHT;

/**
 * @author Mauricio Coria
 */
public class SummaryDiffPrinter implements Printer {
    private static final String durationFmt = "%dms (%3d%%)";
    private static final String searchesFmt = "%d";
    private static final String successRateFmt = "%d%%";
    private static final String evaluationCoincidencesFmt = "%d%%";
    private static final String exploredDepthAvgFmt = "%.1f";
    private static final String nodesPercentageFmt = "%3d%%";
    private static final String nodesFmt = "%d (%3d%%)";
    private static final String evaluatedGamesFmt = "%d (%3d%%)";
    private static final String executedMovesFmt = "%d (%3d%%)";
    private static final String cutoffFmt = "%d%%";
    private static final String pvCompleteFmt = "%d%%";

    private static final String ttReadFmt = "%d (%3d%%)";
    private static final String ttReadHitsFmt = "%d%%";

    private static final String ttWritesFmt = "%d (%3d%%)";
    private static final String ttOverWritesFmt = "%d%%";
    private static final String ttUpdatesFmt = "%d%%";


    @Setter
    @Accessors(chain = true)
    private SummaryDiffModel reportModel;


    @Setter
    @Accessors(chain = true)
    private PrintStream out;


    @Override
    public SummaryDiffPrinter print() {
        SummaryModel baseLineSearchSummary = reportModel.baseLineSearchSummary;
        List<SummaryDiffModel.SummaryDiffPair> searchSummaryPairs = reportModel.searchSummaryPairs;

        out.printf("Suite: %s%n", reportModel.suiteName);

        PrinterTxtTable printerTxtTable = new PrinterTxtTable(2 + reportModel.elements).setOut(out);

        PrinterTxtTable.TextAlignment[] alignments = new PrinterTxtTable.TextAlignment[2 + reportModel.elements];
        alignments[0] = LEFT;
        alignments[1] = RIGHT;
        for (int i = 2; i < 2 + reportModel.elements; i++) {
            alignments[i] = RIGHT;
        }
        printerTxtTable.setTextAlignment(alignments);

        List<String> tmp = new LinkedList<>();
        tmp.add("Metric");
        tmp.add(baseLineSearchSummary.sessionid);
        searchSummaryPairs.stream().map(pair -> pair.searchSummary().sessionid).forEach(tmp::add);
        printerTxtTable.setTitles(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Duration");
        tmp.add(String.format(durationFmt, baseLineSearchSummary.duration, 100));
        searchSummaryPairs.stream().map(pair -> String.format(durationFmt, pair.searchSummary().duration, pair.searchSummaryDiff().durationPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Searches");
        tmp.add(String.format(searchesFmt, baseLineSearchSummary.searches));
        searchSummaryPairs.stream().map(pair -> String.format(searchesFmt, pair.searchSummary().searches)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Success");
        tmp.add(String.format(successRateFmt, baseLineSearchSummary.successRate));
        searchSummaryPairs.stream().map(pair -> String.format(successRateFmt, pair.searchSummary().successRate)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Coincidences");
        tmp.add(String.format(evaluationCoincidencesFmt, 100));
        searchSummaryPairs.stream().map(pair -> String.format(evaluationCoincidencesFmt, pair.searchSummaryDiff().evaluationCoincidencePercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Depth");
        tmp.add(String.format(exploredDepthAvgFmt, baseLineSearchSummary.exploredDepthAvg));
        searchSummaryPairs.stream().map(pair -> String.format(exploredDepthAvgFmt, pair.searchSummary().exploredDepthAvg)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Moves");
        tmp.add(String.format(executedMovesFmt, baseLineSearchSummary.executedMovesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(executedMovesFmt, pair.searchSummary().executedMovesTotal, pair.searchSummaryDiff().executedMovesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));


        tmp.clear();
        tmp.add("Nodes");
        tmp.add(String.format(nodesFmt, baseLineSearchSummary.nodes, 100));
        searchSummaryPairs.stream().map(pair -> String.format(nodesFmt, pair.searchSummary().nodes, pair.searchSummaryDiff().nodesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("INodes");
        tmp.add(String.format(nodesPercentageFmt, baseLineSearchSummary.interiorNodeCounterPercentage));
        searchSummaryPairs.stream().map(pair -> String.format(nodesPercentageFmt, pair.searchSummary().interiorNodeCounterPercentage)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("QNodes");
        tmp.add(String.format(nodesPercentageFmt, baseLineSearchSummary.quiescenceNodeCounterPercentage));
        searchSummaryPairs.stream().map(pair -> String.format(nodesPercentageFmt, pair.searchSummary().quiescenceNodeCounterPercentage)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("LeNodes");
        tmp.add(String.format(nodesPercentageFmt, baseLineSearchSummary.leafNodeCounterPercentage));
        searchSummaryPairs.stream().map(pair -> String.format(nodesPercentageFmt, pair.searchSummary().leafNodeCounterPercentage)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));


        tmp.clear();
        tmp.add("Cutoff");
        tmp.add(String.format(cutoffFmt, baseLineSearchSummary.cutoffPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(cutoffFmt, pair.searchSummary().cutoffPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("PV Complete");
        tmp.add(String.format(pvCompleteFmt, baseLineSearchSummary.pvCompletePercentageAvg));
        searchSummaryPairs.stream().map(pair -> String.format(pvCompleteFmt, pair.searchSummary().pvCompletePercentageAvg)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Evaluations");
        tmp.add(String.format(evaluatedGamesFmt, baseLineSearchSummary.evaluationCounterTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(evaluatedGamesFmt, pair.searchSummary().evaluationCounterTotal, pair.searchSummaryDiff().evaluatedGamesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("Collisions");
        tmp.add(String.format(cutoffFmt, baseLineSearchSummary.evaluationCollisionPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(cutoffFmt, pair.searchSummary().evaluationCollisionPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT Reads");
        tmp.add(String.format(ttReadFmt, baseLineSearchSummary.ttReadsTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(ttReadFmt, pair.searchSummary().ttReadsTotal, pair.searchSummaryDiff().ttReadsPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT Read NHits");
        tmp.add(String.format(ttReadHitsFmt, baseLineSearchSummary.ttReadNodeHitPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(ttReadHitsFmt, pair.searchSummary().ttReadNodeHitPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT Read CHits");
        tmp.add(String.format(ttReadHitsFmt, baseLineSearchSummary.ttReadComparatorHitPercentage));
        searchSummaryPairs.stream().map(pair -> String.format(ttReadHitsFmt, pair.searchSummary().ttReadComparatorHitPercentage)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT Writes");
        tmp.add(String.format(ttWritesFmt, baseLineSearchSummary.ttWritesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(ttWritesFmt, pair.searchSummary().ttWritesTotal, pair.searchSummaryDiff().ttWritesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT Updates");
        tmp.add(String.format(ttUpdatesFmt, baseLineSearchSummary.ttUpdatesPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(ttUpdatesFmt, pair.searchSummary().ttUpdatesPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp.clear();
        tmp.add("TT OverWrites");
        tmp.add(String.format(ttOverWritesFmt, baseLineSearchSummary.ttOverWritesPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(ttOverWritesFmt, pair.searchSummary().ttOverWritesPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        printerTxtTable.print();

        return this;
    }

}
