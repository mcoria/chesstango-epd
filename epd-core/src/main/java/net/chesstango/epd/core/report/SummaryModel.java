package net.chesstango.epd.core.report;


import com.fasterxml.jackson.annotation.JsonProperty;
import net.chesstango.board.representations.move.SimpleMoveEncoder;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;
import net.chesstango.reports.search.nodes.visited.NodesVisitedModel;
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
public class SummaryModel implements Model<EpdAgregateModel> {

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

    @JsonProperty("exploredDepthAvg")
    float exploredDepthAvg;

    @JsonProperty("rootNodes")
    long rootNodes;

    @JsonProperty("interiorNodes")
    long interiorNodes;

    @JsonProperty("quiescenceNodes")
    long quiescenceNodes;

    @JsonProperty("leafNodes")
    long leafNodes;

    @JsonProperty("terminalNodes")
    long terminalNodes;

    @JsonProperty("loopNodes")
    long loopNodes;

    @JsonProperty("egtbNodes")
    long egtbNodes;

    @JsonProperty("nodes")
    long nodes;

    @JsonProperty("cutoffPercentageTotal")
    int cutoffPercentageTotal;

    @JsonProperty("evaluationCounterTotal")
    long evaluationCounterTotal;

    @JsonProperty("evaluationCollisionPercentageTotal")
    int evaluationCollisionPercentageTotal;

    @JsonProperty("pvAccuracyAvgPercentageTotal")
    int pvAccuracyAvgPercentageTotal;

    @JsonProperty("ttReadsTotal")
    long ttReadsTotal;

    @JsonProperty("ttReadHitsPercentageTotal")
    int ttReadHitsPercentageTotal;

    @JsonProperty("ttWritesTotal")
    long ttWritesTotal;

    @JsonProperty("ttUpdatesPercentageTotal")
    int ttUpdatesPercentageTotal;

    @JsonProperty("ttOverWritesPercentageTotal")
    int ttOverWritesPercentageTotal;

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
    public SummaryModel collectStatistics(String sessionId, EpdAgregateModel input) {
        List<EpdSearchResult> epdSearchResults = input.epdSearchResults();
        EpdSearchModel epdSearchModel = input.epdSearchModel();
        NodesVisitedModel nodesVisitedModel = input.nodesVisitedModel();
        NodesTypesModel nodesTypesModel = input.nodesTypesModel();
        EvaluationModel evaluationReportModel = input.evaluationReportModel();
        PrincipalVariationModel principalVariationReportModel = input.principalVariationReportModel();
        TranspositionModel transpositionModel = input.transpositionModel();
        BoardModel boardModel = input.boardModel();


        this.sessionid = sessionId;
        this.duration = epdSearchModel.duration;
        this.searches = epdSearchModel.searches;

        this.success = epdSearchModel.success;
        this.successRate = epdSearchModel.successRate;
        this.depthAccuracyAvgPercentageTotal = epdSearchModel.depthAccuracyAvgPercentageTotal;

        this.executedMovesTotal = boardModel.executedMovesTotal;
        this.exploredDepthAvg =  boardModel.exploredDepthAvg;

        this.nodes = nodesVisitedModel.visitedNodesTotal;
        this.cutoffPercentageTotal = nodesVisitedModel.cutoffPercentageTotal;

        this.rootNodes = nodesTypesModel.rootNodeCounterTotal;
        this.interiorNodes = nodesTypesModel.interiorNodeCounterTotal;
        this.quiescenceNodes = nodesTypesModel.quiescenceNodeCounterTotal;
        this.leafNodes = nodesTypesModel.leafNodeCounterTotal;
        this.terminalNodes = nodesTypesModel.terminalNodeCounterTotal;
        this.loopNodes = nodesTypesModel.loopNodeCounterTotal;
        this.egtbNodes = nodesTypesModel.egtbNodeCounterTotal;

        this.evaluationCounterTotal = evaluationReportModel.evaluationCounterTotal;
        this.evaluationCollisionPercentageTotal = evaluationReportModel.evaluationCollisionPercentageTotal;
        this.pvAccuracyAvgPercentageTotal = principalVariationReportModel.pvAccuracyAvgPercentageTotal;

        this.ttReadsTotal = transpositionModel.readsTotal;
        this.ttReadHitsPercentageTotal = transpositionModel.readHitPercentageTotal;

        this.ttWritesTotal = transpositionModel.writesTotal;
        this.ttUpdatesPercentageTotal = transpositionModel.updatesPercentageTotal;
        this.ttOverWritesPercentageTotal = transpositionModel.overWritesPercentageTotal;

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