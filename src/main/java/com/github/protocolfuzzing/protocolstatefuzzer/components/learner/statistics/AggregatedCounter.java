package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.filter.statistic.Counter;

import java.util.List;

public class AggregatedCounter extends Counter {

    private final List<Counter> counters;

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
