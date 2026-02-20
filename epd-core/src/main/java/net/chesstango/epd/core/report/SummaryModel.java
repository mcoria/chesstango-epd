package net.chesstango.epd.core.report;


import com.fasterxml.jackson.annotation.JsonProperty;
import net.chesstango.board.representations.move.SimpleMoveEncoder;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.NodesModel;
import net.chesstango.reports.search.pv.PrincipalVariationModel;
import net.chesstango.reports.search.transposition.TranspositionModel;
import net.chesstango.search.SearchResult;
import net.chesstango.search.SearchResultByDepth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Mauricio Coria
 */
public class SummaryModel implements Model<SummaryModelInput> {

    @JsonProperty("sessionid")
    String sessionid;

    @JsonProperty("duration")
    long duration;

    @JsonProperty("searches")
    int searches;

    @JsonProperty("success")
    int success;

    @JsonProperty("successRate")
    int successRate;

    @JsonProperty("depthAccuracyAvgPercentageTotal")
    int depthAccuracyAvgPercentageTotal;

    @JsonProperty("executedMovesTotal")
    long executedMovesTotal;

    @JsonProperty("maxSearchRLevel")
    int maxSearchRLevel;

    @JsonProperty("maxSearchQLevel")
    int maxSearchQLevel;

    @JsonProperty("visitedRNodesTotal")
    long visitedRNodesTotal;

    @JsonProperty("visitedQNodesTotal")
    long visitedQNodesTotal;

    @JsonProperty("visitedNodesTotal")
    long visitedNodesTotal;

    @JsonProperty("cutoffPercentageTotal")
    int cutoffPercentageTotal;

    @JsonProperty("evaluationCounterTotal")
    long evaluationCounterTotal;

    @JsonProperty("evaluationCollisionPercentageTotal")
    int evaluationCollisionPercentageTotal;

    @JsonProperty("pvAccuracyAvgPercentageTotal")
    int pvAccuracyAvgPercentageTotal;

    @JsonProperty("overWritePercentageTotal")
    int overWritePercentageTotal;

    @JsonProperty("searchDetail")
    List<SearchSummaryModeDetail> searchDetailList = new LinkedList<>();

    public static class SearchSummaryModeDetail {
        @JsonProperty("id")
        public String id;

        @JsonProperty("move")
        public String move;

        @JsonProperty("success")
        public boolean success;

        @JsonProperty("depthMoves")
        public String depthMoves;

        @JsonProperty("depthAccuracyPercentage")
        public int depthAccuracyPercentage;

        @JsonProperty("pv")
        public String pv;

        @JsonProperty("pvAccuracyPercentage")
        public int pvAccuracyPercentage;

        @JsonProperty("evaluation")
        public int evaluation;
    }


    @Override
    public SummaryModel collectStatistics(String sessionId, SummaryModelInput input) {
        List<EpdSearchResult> epdSearchResults = input.epdSearchResults();
        EpdSearchModel epdSearchModel = input.epdSearchModel();
        NodesModel nodesReportModel = input.nodesReportModel();
        EvaluationModel evaluationReportModel = input.evaluationReportModel();
        PrincipalVariationModel principalVariationReportModel = input.principalVariationReportModel();
        TranspositionModel transpositionModel = input.transpositionModel();

        this.sessionid = sessionId;
        this.duration = epdSearchModel.duration;
        this.searches = epdSearchModel.searches;

        this.success = epdSearchModel.success;
        this.successRate = epdSearchModel.successRate;
        this.depthAccuracyAvgPercentageTotal = epdSearchModel.depthAccuracyAvgPercentageTotal;

        this.maxSearchRLevel = nodesReportModel.maxSearchRLevel;
        this.maxSearchQLevel = nodesReportModel.maxSearchQLevel;

        this.visitedRNodesTotal = nodesReportModel.visitedRNodesTotal;
        this.visitedQNodesTotal = nodesReportModel.visitedQNodesTotal;
        this.visitedNodesTotal = nodesReportModel.visitedNodesTotal;
        this.executedMovesTotal = nodesReportModel.executedMovesTotal;
        this.cutoffPercentageTotal = nodesReportModel.cutoffPercentageTotal;
        this.evaluationCounterTotal = evaluationReportModel.evaluationCounterTotal;
        this.evaluationCollisionPercentageTotal = evaluationReportModel.evaluationCollisionPercentageTotal;
        this.pvAccuracyAvgPercentageTotal = principalVariationReportModel.pvAccuracyAvgPercentageTotal;
        this.overWritePercentageTotal = transpositionModel.overWritePercentageTotal;

        Map<String, PrincipalVariationModel.PrincipalVariationReportModelDetail> pvMap = new HashMap<>();
        principalVariationReportModel.moveDetails.forEach(pvMoveDetail -> pvMap.put(pvMoveDetail.id, pvMoveDetail));

        SimpleMoveEncoder simpleMoveEncoder = new SimpleMoveEncoder();

        epdSearchResults
                .stream()
                .map(epdSearchResult -> {
                    SearchSummaryModeDetail searchSummaryModeDetail = new SearchSummaryModeDetail();
                    SearchResult searchResult = epdSearchResult.getSearchResult();
                    PrincipalVariationModel.PrincipalVariationReportModelDetail pvDetail = pvMap.get(epdSearchResult.getEpd().getId());

                    searchSummaryModeDetail.id = epdSearchResult.getEpd().getId();
                    searchSummaryModeDetail.move = epdSearchResult.getBestMoveFound();
                    searchSummaryModeDetail.success = epdSearchResult.isSearchSuccess();
                    searchSummaryModeDetail.depthMoves = searchResult.getSearchResultByDepths().stream().map(SearchResultByDepth::getBestMove).map(simpleMoveEncoder::encode).toList().toString();
                    searchSummaryModeDetail.depthAccuracyPercentage = epdSearchResult.getDepthAccuracyPct();
                    searchSummaryModeDetail.pv = pvDetail.principalVariation;
                    searchSummaryModeDetail.pvAccuracyPercentage = pvDetail.pvAccuracyPercentage;
                    searchSummaryModeDetail.evaluation = searchResult.getBestEvaluation();
                    return searchSummaryModeDetail;
                })
                .forEach(this.searchDetailList::add);


        return this;
    }
}