package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;

/**
 * Standard implementation of the StatisticsTracker for specific input domain
 * and counterexample type.
 *
 * @param <B>  the type of base symbols
 * @param <D>  the domain of inputs and outputs
 * @param <OD> the type of output domain
 */
public class StatisticsTrackerRA<B, D, OD> extends StatisticsTracker<B, Word<D>, OD, DefaultQuery<D, OD>> {

    /**
     * Creates a new instance from the given parameters.
     *
     * @param inputCounter counter updated on every input of membership and
     *                     equivalence queries
     * @param testCounter  counter updated on every membership and equivalence query
     *                     (also named test)
     */
    public StatisticsTrackerRA(Counter inputCounter, Counter testCounter) {
        super(inputCounter, testCounter);
    }

    @Override
    protected Word<D> getInputOfCE(DefaultQuery<D, OD> counterexample) {
        return counterexample.getInput();
    }

    @Override
    protected OD getOutputOfCE(DefaultQuery<D, OD> counterexample) {
        return counterexample.getOutput();
    }
}
