package net.chesstango.epd.core.search;

import net.chesstango.evaluation.Evaluator;
import net.chesstango.evaluation.evaluators.EvaluatorByMaterial;
import net.chesstango.search.Search;
import net.chesstango.search.builders.AlphaBetaBuilder;

import java.util.function.Supplier;

import static net.chesstango.search.alphabeta.Constants.DEFAULT_TT_HASH_SIZE_KB;
import static net.chesstango.search.alphabeta.Constants.DEFAULT_TT_STALE_AGE;

/**
 * @author Mauricio Coria
 */
public class SearchSupplier implements Supplier<Search> {

    @Override
    public Search get() {
        return conPoco();
    }

    static Search createDefault() {
        return AlphaBetaBuilder
                .createDefaultBuilderInstance()
                .withGameEvaluator(Evaluator.createInstance())
                .withStatistics()
                .build();
    }

    static Search createNoTranspositionTable() {
        return new AlphaBetaBuilder()
                // START createDefaultBuilderInstance() pero sin TT
                .withGameEvaluatorCache()

                .withQuiescence()

                .withKillerMoveSorter()
                .withRecaptureSorter()
                .withMvvLvaSorter()

                .withAspirationWindows()

                .withIterativeDeepening()

                .withStopProcessingCatch()
                // FIN

                .withGameEvaluator(Evaluator.createInstance())
                .withStatistics()

                .build();
    }

    static Search conPoco() {
        return new AlphaBetaBuilder()
                // START createDefaultBuilderInstance()
                .withQuiescence()

                .withKillerMoveSorter()
                .withRecaptureSorter()
                .withMvvLvaSorter()

                .withAspirationWindows()

                .withIterativeDeepening()

                .withStopProcessingCatch()
                // FIN


                .withStatistics()
                .withGameEvaluator(Evaluator.createInstance())
                .withStatistics()

                .build();
    }


}
