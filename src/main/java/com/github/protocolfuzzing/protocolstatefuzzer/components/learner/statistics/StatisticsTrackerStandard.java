package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;

/**
 * Standard implementation of the StatisticsTracker for specific input domain
 * and counterexample type.
 *
 * @param <I>   the type of inputs
 * @param <OD>  the type of output domain
 */
public class StatisticsTrackerStandard<I, OD> extends StatisticsTracker<I, Word<I>, OD, DefaultQuery<I, OD>> {

    /**
     * Creates a new instance from the given parameters.
     *
     * @param inputCounter  counter updated on every input of membership and equivalence queries
     * @param testCounter   counter updated on every membership and equivalence query (also named test)
     */
    public StatisticsTrackerStandard(Counter inputCounter, Counter testCounter) {
        super(inputCounter, testCounter);
    }

    @Override
    protected Word<I> getInputOfCE(DefaultQuery<I, OD> counterexample) {
        return counterexample.getInput();
    }

    @Override
    protected OD getOutputOfCE(DefaultQuery<I, OD> counterexample) {
        return counterexample.getOutput();
    }
}
