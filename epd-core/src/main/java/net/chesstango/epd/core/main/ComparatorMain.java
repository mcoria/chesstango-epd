package net.chesstango.epd.core.main;


import net.chesstango.epd.core.report.SummaryDiffModel;
import net.chesstango.epd.core.report.SummaryDiffModelInput;
import net.chesstango.epd.core.report.SummaryDiffReport;
import net.chesstango.epd.core.report.SummaryModel;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class ComparatorMain {

    public static void main(String[] args) {
        printSummaryLegends();

        Path suiteParentDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\EPD\\database");

        ComparatorMain comparatorMain = new ComparatorMain(suiteParentDirectory, "depth-5-2026-03-31-08-20-v1.6.0");
        //comparatorMain.addSession("depth-7-2026-08-09-22-38-v1.7.1");
        //comparatorMain.addSession("depth-7-2026-08-18-13-35-v1.8.0-SNAPSHOT");
        comparatorMain.addSession("depth-5-2026-08-18-10-38-v1.8.0-SNAPSHOT");
        comparatorMain.addSession("depth-5-2026-08-20-08-12-v1.8.0-SNAPSHOT");
        comparatorMain.addSession("depth-5-2026-08-20-10-19-v1.8.0-SNAPSHOT");
        //

        //
        comparatorMain.execute();
    }

    private static void printSummaryLegends() {
        String content = """
                Metric description:
                Duration         (ms): milliseconds spent in the search phase.
                Searches             : number of searches performed.
                Moves Success     (%): percentage of successful moves.
                Evals Success     (%): percentage of successful evaluations.
                DepthAvg             : average depth reached.
                Moves                : executed moves.
                Max  Level           : Max depth reached.
                Vis  Nodes           : Visited nodes.
                Cutoff            (%): Cutoff percentage.
                PV Complete       (%): Principal variation complete percentage.
                Evaluations          : evaluations performed.
                 Coincidences     (%): percentage of evaluations that are coincidences with baseline.
                 Collisions       (%): Different positions with same evaluation (Collisions).
                TT ReadHits          : TT reads.
                TT Read NHits     (%): TT Node Reads.
                TT Read CHits     (%): TT Comparator Reads.
                TT Writes            : TT writes.
                TT Updates        (%): TT updates percentage.
                TT OverWrites     (%): TT overwrites percentage.
                """;

        System.out.println(content);
    }

    private final Path suiteParentDirectory;
    private final String baseLineSessionID;
    private final List<String> searchSessions = new ArrayList<>();

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private String suiteName;
    private SummaryModel baseLineSearchSummary;
    private List<SummaryModel> searchSummaryList;

    public ComparatorMain(Path suiteParentDirectory, String baseLineSessionID) {
        this.suiteParentDirectory = suiteParentDirectory;
        this.baseLineSessionID = baseLineSessionID;
    }

    public void addSession(String sessionId) {
        searchSessions.add(sessionId);
    }

    public void execute() {
        Path startPath = suiteParentDirectory.resolve(baseLineSessionID);
        String extension = ".json";

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(extension))
                    .map(suitePath -> suitePath.getFileName().toString())
                    .map(suiteNameWithExtension -> suiteNameWithExtension.substring(0, suiteNameWithExtension.length() - extension.length()))
                    .forEach(suiteName -> {
                        loadSearchSummaries(suiteName);
                        printReport(System.out);
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadSearchSummaries(String theSuiteName) {
        suiteName = theSuiteName;

        baseLineSearchSummary = loadSearchSummary(baseLineSessionID);

        if (baseLineSearchSummary == null) {
            throw new RuntimeException("baseLineSearchSummary not found");
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

        return objectMapper.readValue(searchSummaryPath.toFile(), SummaryModel.class);
    }

    private void printReport(PrintStream out) {
        SummaryDiffModel reportModel = new SummaryDiffModel().collectStatistics(suiteName, new SummaryDiffModelInput(baseLineSearchSummary, searchSummaryList));

        new SummaryDiffReport()
                .withSummaryDiffReportModel(reportModel)
                .printReport(out);
    }
}



