package net.chesstango.epd.core.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chesstango.epd.core.report.SummaryDiffReport;
import net.chesstango.epd.core.report.SummaryDiffModel;
import net.chesstango.epd.core.report.SummaryModel;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mauricio Coria
 */
public class EpdSearchComparatorMain {

    public static void main(String[] args) {
        printSummaryLegends();

        EpdSearchComparatorMain epdSearchComparatorMain = new EpdSearchComparatorMain("depth-7-2026-02-19-08-51-v1.4.0-SNAPSHOT");
        epdSearchComparatorMain.addSession("depth-8-2026-02-19-09-37-v1.4.0-SNAPSHOT");
        //epdSearchComparatorMain.addSession("depth-7-2026-02-19-08-51-v1.4.0-SNAPSHOT");


        //

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-w1.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-b1.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-w2.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-b2.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-w3.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\mate-b3.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\Bratko-Kopec.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\Kaufman.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\wac-2018.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\sbd.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\Nolot.epd");

        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS1.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS2.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS3.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS4.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS5.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS6.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS7.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS8.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS9.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS10.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS11.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS12.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS13.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS14.epd");
        epdSearchComparatorMain.execute("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database\\STS15.epd");
    }

    private static void printSummaryLegends() {
        String content = """
            Metric description:
            Duration (ms)   : milliseconds spent in the search phase.
            Searches        : number of searches performed.
            Success      (%): percentage of successful moves.
            Coincidences (%): percentage of coincidences between evaluations.
            Dpt Accuracy (%): AVG percentage of the best moves found at each depth level that match the expected successful moves defined in the EPD position.  
            Exec Moves      : executed moves.
            Evaluations     : evaluations performed.
             Collisions  (%): Different positions with same evaluation (Collisions).
            Max RLevel      : Max regular depth reached.
            Max QLevel      : Max quiscence depth reached.
            Vis RNodes      : Visited nodes at regular depth.
            Vis QNodes      : Visited nodes at quiscence depth.
            Vis  Nodes      : Visited nodes.
            Cutoff       (%): Cutoff percentage.
            PV Accuracy  (%): Principal variation accuracy percentage.
            """;
        System.out.println(content);
    }

    private final String baseLineSessionID;
    private final List<String> searchSessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path suiteParentDirectory;
    private String suiteName;
    private SummaryModel baseLineSearchSummary;
    private List<SummaryModel> searchSummaryList;

    public EpdSearchComparatorMain(String baseLineSessionID) {
        this.baseLineSessionID = baseLineSessionID;
    }

    private void execute(String suiteFile) {
        loadSearchSummaries(suiteFile);
        printReport(System.out);
    }

    private void printReport(PrintStream out) {
        SummaryDiffModel reportModel = SummaryDiffModel.createModel(suiteName, baseLineSearchSummary, searchSummaryList);

        new SummaryDiffReport()
                .withSummaryDiffReportModel(reportModel)
                .printReport(out);
    }


    private void loadSearchSummaries(String suiteFile) {
        Path suitePath = Paths.get(suiteFile);

        if (!Files.exists(suitePath)) {
            System.err.printf("file not found: %s\n", suiteFile);
            return;
        }

        suiteName = suitePath.getFileName().toString();

        suiteParentDirectory = suitePath.getParent();

        baseLineSearchSummary = loadSearchSummary(baseLineSessionID);

        if (baseLineSearchSummary == null) {
            System.err.printf("baseLineSearchSummary not found: %s\n", suiteName);
            return;
        }

        searchSummaryList = searchSessions.stream()
                .map(this::loadSearchSummary)
                .filter(Objects::nonNull)
                .toList();
    }

    private SummaryModel loadSearchSummary(String sessionID) {

        Path searchSummaryPath = suiteParentDirectory.resolve(sessionID).resolve(String.format("%s.json", suiteName));

        if (!Files.exists(searchSummaryPath)) {
            System.err.printf("file not found: %s\n", searchSummaryPath);
            return null;
        }

        try {
            return objectMapper.readValue(searchSummaryPath.toFile(), SummaryModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSession(String sessionID) {
        searchSessions.add(sessionID);
    }
}



