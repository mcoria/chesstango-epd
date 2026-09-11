package net.chesstango.epd.core.report;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.reports.Printer;
import net.chesstango.reports.PrinterTxtTable;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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
    private static final String ttFillAvgFmt = "%3d%%";




    @Setter
    @Accessors(chain = true)
    private SummaryDiffModel reportModel;


    @Setter
    @Accessors(chain = true)
    private PrintStream out;


    @Override
    public SummaryDiffPrinter print() {
        List<String> tmp = new LinkedList<>();
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

        printerTxtTable.setTitles(createStringRow("Metric", "%s", summaryModel -> summaryModel.sessionid));

        printerTxtTable.addRow(createPercentageRow("Duration", durationFmt, SummaryModel::getDuration, SummaryDiffModel.SearchSummaryDiff::durationPercentage));

        printerTxtTable.addRow(createStringRow("Searches", searchesFmt, summaryModel -> summaryModel.searches));

        printerTxtTable.addRow(createStringRow("Success", "%s", _ -> ""));

        printerTxtTable.addRow(createStringRow(" Move", successRateFmt, summaryModel -> summaryModel.moveSuccessPct));

        printerTxtTable.addRow(createStringRow(" Evaluation", successRateFmt, summaryModel -> summaryModel.evaluationSuccessPct));

        printerTxtTable.addRow(createStringRow("DepthAvg", exploredDepthAvgFmt, summaryModel -> summaryModel.exploredDepthAvg));

        printerTxtTable.addRow(createPercentageRow("Moves", executedMovesFmt, SummaryModel::getExecutedMovesTotal, SummaryDiffModel.SearchSummaryDiff::executedMovesPercentage));

        printerTxtTable.addRow(createPercentageRow("Nodes", nodesFmt, SummaryModel::getNodes, SummaryDiffModel.SearchSummaryDiff::nodesPercentage));

        printerTxtTable.addRow(createStringRow(" Internal", nodesPercentageFmt, summaryModel -> summaryModel.interiorNodeCounterPercentage));

        printerTxtTable.addRow(createStringRow(" Quiescence", nodesPercentageFmt, summaryModel -> summaryModel.quiescenceNodeCounterPercentage));

        printerTxtTable.addRow(createStringRow(" Leaf", nodesPercentageFmt, summaryModel -> summaryModel.leafNodeCounterPercentage));

        printerTxtTable.addRow(createStringRow("Cutoff", cutoffFmt, summaryModel -> summaryModel.cutoffPercentageTotal));

        printerTxtTable.addRow(createStringRow("PV complete", pvCompleteFmt, summaryModel -> summaryModel.pvCompletePercentageAvg));

        printerTxtTable.addRow(createPercentageRow("Evaluations", evaluatedGamesFmt, SummaryModel::getEvaluationCounterTotal, SummaryDiffModel.SearchSummaryDiff::evaluatedGamesPercentage));

        tmp.clear();
        tmp.add(" Coincidences");
        tmp.add(String.format(evaluationCoincidencesFmt, 100));
        searchSummaryPairs.stream().map(pair -> String.format(evaluationCoincidencesFmt, pair.searchSummaryDiff().evaluationCoincidencePercentage())).forEach(tmp::add);
        printerTxtTable.addRow(tmp.toArray(new String[0]));

        printerTxtTable.addRow(createStringRow(" Collisions", cutoffFmt, SummaryModel::getEvaluationCollisionPercentageTotal));

        printerTxtTable.addRow(createStringRow("TT Node", "%s", _ -> ""));

        printerTxtTable.addRow(createPercentageRow(" Reads", ttReadFmt, SummaryModel::getTtReadsNodeTotal, SummaryDiffModel.SearchSummaryDiff::ttReadsPercentage));

        printerTxtTable.addRow(createStringRow(" Reads NHits", ttReadHitsFmt, SummaryModel::getTtReadNodeHitPercentageTotal));

        printerTxtTable.addRow(createPercentageRow(" Writes", ttWritesFmt, SummaryModel::getTtWritesTotal, SummaryDiffModel.SearchSummaryDiff::ttWritesPercentage));

        printerTxtTable.addRow(createStringRow(" Updates", ttUpdatesFmt, SummaryModel::getTtUpdatesPercentageTotal));

        printerTxtTable.addRow(createStringRow(" OverWrites", ttOverWritesFmt, SummaryModel::getTtOverWritesPercentageTotal));

        printerTxtTable.addRow(createStringRow(" Fill Avg", ttFillAvgFmt, SummaryModel::getTtMapFillPercentageAvg));

        printerTxtTable.addRow(createStringRow("TT Comparator", "%s", _ -> ""));

        printerTxtTable.addRow(createPercentageRow(" Reads", ttReadFmt, SummaryModel::getTtReadComparatorTotal, SummaryDiffModel.SearchSummaryDiff::ttComparatorPercentage));

        printerTxtTable.addRow(createStringRow(" Reads CHits", ttReadHitsFmt, SummaryModel::getTtReadComparatorHitPercentage));

        printerTxtTable.addRow(createStringRow("EvalCache", "%s", _ -> ""));

        printerTxtTable.addRow(createPercentageRow(" Reads Nodes", ttReadFmt, SummaryModel::getEvalCacheReadNodeTotal, SummaryDiffModel.SearchSummaryDiff::evalCacheReadNodesPercentage));

        printerTxtTable.addRow(createStringRow(" Reads NHits", ttReadHitsFmt, SummaryModel::getEvalCacheReadNodeHitsPercentageTotal));

        printerTxtTable.addRow(createPercentageRow(" Reads Comparator", ttReadFmt, SummaryModel::getEvalCacheReadComparatorsTotal, SummaryDiffModel.SearchSummaryDiff::evalCacheReadComparatorsPercentage));

        printerTxtTable.addRow(createStringRow(" Reads CHits", ttReadHitsFmt, SummaryModel::getEvalCacheReadComparatorHitsPercentageTotal));

        printerTxtTable.addRow(createStringRow(" Fill Avg", ttFillAvgFmt, SummaryModel::getEvalCacheReadFillPercentageAvg));

        printerTxtTable.print();

        return this;
    }

    String[] createStringRow(String metric, String format, Function<SummaryModel, Object> smToStr) {
        SummaryModel baseLineSearchSummary = reportModel.baseLineSearchSummary;
        List<SummaryDiffModel.SummaryDiffPair> searchSummaryPairs = reportModel.searchSummaryPairs;

        List<String> tmp = new LinkedList<>();
        tmp.add(metric);
        tmp.add(String.format(format, smToStr.apply(baseLineSearchSummary)));
        searchSummaryPairs
                .stream()
                .map(SummaryDiffModel.SummaryDiffPair::searchSummary)
                .map(summary -> String.format(format, smToStr.apply(summary)))
                .forEach(tmp::add);

        return tmp.toArray(new String[0]);
    }

    String[] createPercentageRow(String metric, String format, Function<SummaryModel, Number> smToNumber, Function<SummaryDiffModel.SearchSummaryDiff, Number> sdToNumber) {
        SummaryModel baseLineSearchSummary = reportModel.baseLineSearchSummary;
        List<SummaryDiffModel.SummaryDiffPair> searchSummaryPairs = reportModel.searchSummaryPairs;

        List<String> tmp = new LinkedList<>();
        tmp.add(metric);
        tmp.add(String.format(format, smToNumber.apply(baseLineSearchSummary), 100));
        searchSummaryPairs
                .stream()
                .map(pair -> String.format(format, smToNumber.apply(pair.searchSummary()), sdToNumber.apply(pair.searchSummaryDiff())))
                .forEach(tmp::add);

        return tmp.toArray(new String[0]);
    }

}
