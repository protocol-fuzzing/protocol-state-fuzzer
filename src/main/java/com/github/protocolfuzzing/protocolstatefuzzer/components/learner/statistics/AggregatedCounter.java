package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.filter.statistic.Counter;

import java.util.List;

/**
 * Aggregates multiple counters for parallel execution scenarios.
 * <p>
 * The aggregated counter only supports read operations ({@link #getCount()}).
 * Write operations ({@link #increment()}) should be performed on the individual
 * counters that make up this aggregation.
 * </p>
 */
public class AggregatedCounter extends Counter {

    /** The list of individual counters to aggregate. */
    private final List<Counter> counters;

    /**
     * Constructs a new AggregatedCounter with the specified list of counters.
     * <p>
     * The aggregated counter will sum the counts from all provided counters
     * when {@link #getCount()} is called.
     * </p>
     *
     * @param counters  a list of {@link Counter} instances to aggregate.
     */
    public AggregatedCounter(List<Counter> counters) {
        super("AggregatedCounter", "#");
        this.counters = counters;
    }

    @Override
    public long getCount() {
        long total = 0;
        for (Counter counter : counters) {
            total += counter.getCount();
        }
        return total;
    }

    @Override
    public void increment() {
        throw new UnsupportedOperationException(
                "Increment should be called on individual counters, not on the aggregated counter"
        );
    }
}
