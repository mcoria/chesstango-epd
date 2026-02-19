package net.chesstango.epd.core.report;

import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.ReportToFile;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;

import java.nio.file.Path;
import java.util.List;

import static net.chesstango.epd.core.main.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
public class EpdSearchReportSaver {
    private final ReportToFile reportToFile;

    public EpdSearchReportSaver(Path directory) {
        this.reportToFile = new ReportToFile(directory);
    }

    public void saveReports(String suiteName, List<EpdSearchResult> epdSearchResults) {
        EpdSearchModel epdSearchModel = EpdSearchModel.collectStatistics(suiteName, epdSearchResults);
        NodesModel nodesReportModel = NodesModel.collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        EvaluationModel evaluationReportModel = EvaluationModel.collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        PrincipalVariationModel principalVariationReportModel = PrincipalVariationModel.collectStatics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        TranspositionModel transpositionReportModel = TranspositionModel.collectStatistics(suiteName, epdSearchResults.stream().map(EpdSearchResult::getSearchResult).toList());
        SummaryModel reportModel = SummaryModel.collectStatics(SESSION_DATE, epdSearchResults, epdSearchModel, nodesReportModel, evaluationReportModel, principalVariationReportModel, transpositionReportModel);

        reportToFile.save(String.format("%s-report.txt", suiteName), new EpdAgregateReport()
                .setEvaluationReportModel(evaluationReportModel)
                .setEpdSearchModel(epdSearchModel)
                .setNodesReportModel(nodesReportModel)
                .setPrincipalVariationReportModel(principalVariationReportModel)
                .setTranspositionReportModel(transpositionReportModel)
        );

        reportToFile.save(String.format("%s.json", suiteName), new SummaryReport()
                .setReportModel(reportModel)
        );

    }
}
