package io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.StatisticsKey;
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
     * @param inputStatisticsKey key for counter updated on every input of membership and
     *                               equivalence queries
     * @param testStatisticsKey  key for counter updated on every membership and equivalence query
     *                               (also named test)
     */
    public StatisticsTrackerRA(StatisticsKey inputStatisticsKey, StatisticsKey testStatisticsKey) {
        super(inputStatisticsKey, testStatisticsKey);
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
