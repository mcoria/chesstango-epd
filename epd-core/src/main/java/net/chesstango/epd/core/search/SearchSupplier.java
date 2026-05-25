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
        return createDefaultWithTranspositionStaleAge();
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

                //.withAspirationWindows()

                //.withIterativeDeepening()

                //.withStopProcessingCatch()
                // FIN

                .withGameEvaluator(Evaluator.createInstance())

                .withStatistics()

                .build();
    }


    static Search createDefaultWithTranspositionStaleAge() {
        return AlphaBetaBuilder
                .createDefaultBuilderInstance()
                .withGameEvaluator(Evaluator.createInstance())
                .withTranspositionStaleAge(3)
                .withStatistics()
                .build();
    }

}
