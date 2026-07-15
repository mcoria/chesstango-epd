package net.chesstango.epd.core.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chesstango.epd.core.report.SummaryDiffModel;
import net.chesstango.epd.core.report.SummaryDiffModelInput;
import net.chesstango.epd.core.report.SummaryDiffReport;
import net.chesstango.epd.core.report.SummaryModel;

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
public class SearchComparatorMain {

    public static void main(String[] args) {
        printSummaryLegends();

        Path suiteParentDirectory = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\PGN\\database");

        SearchComparatorMain searchComparatorMain = new SearchComparatorMain(suiteParentDirectory, "depth-5-2026-07-13-00-51-v1.7.0-SNAPSHOT");
        searchComparatorMain.addSession("depth-5-2026-07-13-01-26-v1.7.0-SNAPSHOT");

        //
        searchComparatorMain.execute();
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String suiteName;
    private SummaryModel baseLineSearchSummary;
    private List<SummaryModel> searchSummaryList;

    public SearchComparatorMain(Path suiteParentDirectory, String baseLineSessionID) {
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

        try {
            return objectMapper.readValue(searchSummaryPath.toFile(), SummaryModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printReport(PrintStream out) {
        SummaryDiffModel reportModel = new SummaryDiffModel().collectStatistics(suiteName, new SummaryDiffModelInput(baseLineSearchSummary, searchSummaryList));

        new SummaryDiffReport()
                .withSummaryDiffReportModel(reportModel)
                .printReport(out);
    }
}



