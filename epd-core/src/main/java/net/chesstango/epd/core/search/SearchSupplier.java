package net.chesstango.epd.core.search;

import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.Search;
import net.chesstango.search.builders.AlphaBetaBuilder;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class SearchSupplier implements Supplier<Search> {
    @Override
    public Search get() {
        return createNoTranspositionTable();
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
                .withGameEvaluator(Evaluator.createInstance())
                .withGameEvaluatorCache()

                .withQuiescence()

                .withKillerMoveSorter()
                .withRecaptureSorter()
                .withMvvLvaSorter()

                .withAspirationWindows()

                .withIterativeDeepening()

                .withStopProcessingCatch()

                .withStatistics()

                .build();
    }

}
