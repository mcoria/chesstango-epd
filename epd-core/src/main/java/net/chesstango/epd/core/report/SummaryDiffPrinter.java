package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Printer;
import net.chesstango.reports.PrinterTxtTable;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public class SummaryDiffPrinter implements Printer {
    private static final String durationFmt = "%dms (%3d%%)";
    private static final String searchesFmt = "%d";
    private static final String successRateFmt = "%d%%";
    private static final String evaluationCoincidencesFmt = "%d%%";
    private static final String successLevelFmt = "%d";
    private static final String visitedNodesFmt = "%d (%3d%%)";
    private static final String evaluatedGamesFmt = "%d (%3d%%)";
    private static final String executedMovesFmt = "%d (%3d%%)";
    private static final String accuracyFmt = "%d%%";
    private static final String cutoffFmt = "%d%%";
    private static final String pvAccuracyFmt = "%d%%";
    private static final String overWriteFmt = "%d%%";


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

        List<String> tmp = new LinkedList<>();
        tmp.add("Metric");
        tmp.add(baseLineSearchSummary.sessionid);
        searchSummaryPairs.stream().map(pair -> pair.searchSummary().sessionid).forEach(tmp::add);
        printerTxtTable.setTitles(tmp.toArray(new String[0]));


        tmp = new LinkedList<>();
        tmp.add("Duration");
        tmp.add(String.format(durationFmt, baseLineSearchSummary.duration, 100));
        searchSummaryPairs.stream().map(pair -> String.format(durationFmt, pair.searchSummary().duration, pair.searchSummaryDiff().durationPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Searches");
        tmp.add(String.format(searchesFmt, baseLineSearchSummary.searches));
        searchSummaryPairs.stream().map(pair -> String.format(searchesFmt, pair.searchSummary().searches)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Success");
        tmp.add(String.format(successRateFmt, baseLineSearchSummary.successRate));
        searchSummaryPairs.stream().map(pair -> String.format(successRateFmt, pair.searchSummary().successRate)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Coincidences");
        tmp.add(String.format(evaluationCoincidencesFmt, 100 ));
        searchSummaryPairs.stream().map(pair -> String.format(evaluationCoincidencesFmt, pair.searchSummaryDiff().evaluationCoincidencePercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Dpt Accuracy");
        tmp.add(String.format(accuracyFmt, baseLineSearchSummary.depthAccuracyAvgPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(accuracyFmt, pair.searchSummary().depthAccuracyAvgPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));


        tmp = new LinkedList<>();
        tmp.add("Exec Moves");
        tmp.add(String.format(executedMovesFmt, baseLineSearchSummary.executedMovesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(executedMovesFmt, pair.searchSummary().executedMovesTotal, pair.searchSummaryDiff().executedMovesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Evaluations");
        tmp.add(String.format(evaluatedGamesFmt, baseLineSearchSummary.evaluationCounterTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(evaluatedGamesFmt, pair.searchSummary().evaluationCounterTotal, pair.searchSummaryDiff().evaluatedGamesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Collisions");
        tmp.add(String.format(cutoffFmt, baseLineSearchSummary.evaluationCollisionPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(cutoffFmt, pair.searchSummary().evaluationCollisionPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Max RLevel");
        tmp.add(String.format(successLevelFmt, baseLineSearchSummary.maxSearchRLevel));
        searchSummaryPairs.stream().map(pair -> String.format(successLevelFmt, pair.searchSummary().maxSearchRLevel)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Max QLevel");
        tmp.add(String.format(successLevelFmt, baseLineSearchSummary.maxSearchQLevel));
        searchSummaryPairs.stream().map(pair -> String.format(successLevelFmt, pair.searchSummary().maxSearchQLevel)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Vis RNodes");
        tmp.add(String.format(visitedNodesFmt, baseLineSearchSummary.visitedRNodesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(visitedNodesFmt, pair.searchSummary().visitedRNodesTotal, pair.searchSummaryDiff().visitedRNodesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Vis QNodes");
        tmp.add(String.format(visitedNodesFmt, baseLineSearchSummary.visitedQNodesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(visitedNodesFmt, pair.searchSummary().visitedQNodesTotal, pair.searchSummaryDiff().visitedQNodesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Vis Nodes");
        tmp.add(String.format(visitedNodesFmt, baseLineSearchSummary.visitedNodesTotal, 100));
        searchSummaryPairs.stream().map(pair -> String.format(visitedNodesFmt, pair.searchSummary().visitedNodesTotal, pair.searchSummaryDiff().visitedNodesPercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("Cutoff");
        tmp.add(String.format(cutoffFmt, baseLineSearchSummary.cutoffPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(cutoffFmt, pair.searchSummary().cutoffPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("PV Accuracy");
        tmp.add(String.format(pvAccuracyFmt, baseLineSearchSummary.pvAccuracyAvgPercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(pvAccuracyFmt, pair.searchSummary().pvAccuracyAvgPercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        tmp = new LinkedList<>();
        tmp.add("TT Overwrites");
        tmp.add(String.format(overWriteFmt, baseLineSearchSummary.overWritePercentageTotal));
        searchSummaryPairs.stream().map(pair -> String.format(overWriteFmt, pair.searchSummary().overWritePercentageTotal)).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        printerTxtTable.print();
        return this;
    }

}
