package net.chesstango.epd.core.report;


import com.fasterxml.jackson.annotation.JsonProperty;
import net.chesstango.board.representations.move.SimpleMoveEncoder;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.reports.Model;
import net.chesstango.reports.search.board.BoardModel;
import net.chesstango.reports.search.evaluation.EvaluationModel;
import net.chesstango.reports.search.nodes.depth.NodesDepthModel;
import net.chesstango.reports.search.nodes.types.NodesTypesModel;
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

    @JsonProperty("executedMovesTotal")
    long executedMovesTotal;

    @JsonProperty("exploredDepthAvg")
    float exploredDepthAvg;

    @JsonProperty("interiorNodeCounterPercentage")
    int interiorNodeCounterPercentage;

    @JsonProperty("quiescenceNodeCounterPercentage")
    int quiescenceNodeCounterPercentage;

    @JsonProperty("leafNodeCounterPercentage")
    int leafNodeCounterPercentage;

    @JsonProperty("loopNodes")
    long loopNodes;

    @JsonProperty("nodes")
    long nodes;

    @JsonProperty("cutoffPercentageTotal")
    int cutoffPercentageTotal;

    @JsonProperty("evaluationCounterTotal")
    long evaluationCounterTotal;

    @JsonProperty("evaluationCollisionPercentageTotal")
    int evaluationCollisionPercentageTotal;

    @JsonProperty("pvCompletePercentageAvg")
    int pvCompletePercentageAvg;

    @JsonProperty("ttReadsTotal")
    long ttReadsTotal;

    @JsonProperty("ttReadNodeHitPercentageTotal")
    int ttReadNodeHitPercentageTotal;

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

        @JsonProperty("pvComplete")
        public boolean pvComplete;

        @JsonProperty("evaluation")
        public int evaluation;
    }


    @Override
    public SummaryModel collectStatistics(String sessionId, EpdAgregateModel input) {
        List<EpdSearchResult> epdSearchResults = input.epdSearchResults();
        EpdSearchModel epdSearchModel = input.epdSearchModel();
        NodesDepthModel nodesDepthModel = input.nodesDepthModel();
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

        this.executedMovesTotal = boardModel.executedMovesTotal;
        this.exploredDepthAvg = boardModel.exploredDepthAvg;

        this.nodes = nodesDepthModel.visitedNodesTotal;
        this.cutoffPercentageTotal = nodesDepthModel.cutoffPercentageTotal;

        this.interiorNodeCounterPercentage = nodesTypesModel.interiorNodeCounterPercentage;
        this.quiescenceNodeCounterPercentage = nodesTypesModel.quiescenceNodeCounterPercentage;
        this.leafNodeCounterPercentage = nodesTypesModel.leafNodeCounterPercentage;
        this.loopNodes = nodesTypesModel.loopNodeCounterTotal;

        this.evaluationCounterTotal = evaluationReportModel.evaluationCounterTotal;
        this.evaluationCollisionPercentageTotal = evaluationReportModel.evaluationCollisionPercentageTotal;
        this.pvCompletePercentageAvg = principalVariationReportModel.pvCompletePercentageAvg;

        this.ttReadsTotal = transpositionModel.readsTotal;
        this.ttReadNodeHitPercentageTotal = transpositionModel.readNodeHitPercentageTotal;

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
                    searchSummaryModeDetail.pvComplete = pvDetail.pvComplete;
                    searchSummaryModeDetail.evaluation = searchResult.getBestEvaluation();
                    return searchSummaryModeDetail;
                })
                .forEach(this.searchDetailList::add);


        return this;
    }
}