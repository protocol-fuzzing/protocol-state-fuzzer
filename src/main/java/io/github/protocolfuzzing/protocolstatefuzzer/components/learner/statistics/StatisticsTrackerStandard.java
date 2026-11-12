package io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.StatisticsKey;
import net.automatalib.word.Word;

import java.util.List;

/**
 * Standard implementation of the StatisticsTracker for specific input domain
 * and counterexample type.
 *
 * @param <I>  the type of inputs
 * @param <OD> the type of output domain
 */
public class StatisticsTrackerStandard<I, OD> extends StatisticsTracker<I, Word<I>, OD, DefaultQuery<I, OD>> {

    /**
     * Creates a new instance from the given parameters.
     *
     * @param inputStatisticsKey key for counter updated on every input of membership and equivalence queries
     * @param testStatisticsKey  key for counter updated on every membership and equivalence query (also named test)
     */
    public StatisticsTrackerStandard(StatisticsKey inputStatisticsKey, StatisticsKey testStatisticsKey) {
        super(inputStatisticsKey, testStatisticsKey);
    }

    /**
     * Creates a new instance from the given parameters.
     * <p>
     * This should be used when values of multiple counters should be aggregated to a single value
     *
     * @param inputStatisticsKeys keys for counter updated on every input of membership and equivalence queries
     * @param testStatisticsKeys  keys for counter updated on every membership and equivalence query (also named test)
     */
    public StatisticsTrackerStandard(List<StatisticsKey> inputStatisticsKeys, List<StatisticsKey> testStatisticsKeys) {
        super(inputStatisticsKeys, testStatisticsKeys);
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
