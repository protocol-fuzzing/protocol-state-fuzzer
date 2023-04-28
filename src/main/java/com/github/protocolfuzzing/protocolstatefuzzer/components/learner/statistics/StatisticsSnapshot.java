package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

/**
 * Snapshot of relevant statistics at selected phases during the learning process.
 */
public class StatisticsSnapshot {

    /** Stores the constructor parameter. */
    protected long tests;

    /** Stores the constructor parameter. */
    protected long inputs;

    /** Stores the constructor parameter. */
    protected long time;

    /**
     * Returns the stored value of {@link #tests}.
     *
     * @return  the stored value of {@link #tests}
     */
    public long getTests() {
        return tests;
    }

    /**
     * Returns the stored value of {@link #inputs}.
     *
     * @return  the stored value of {@link #inputs}
     */
    public long getInputs() {
        return inputs;
    }

    /**
     * Returns the stored value of {@link #time}.
     *
     * @return  the stored value of {@link #time}
     */
    public long getTime() {
        return time;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param tests   the number of tests (queries) used
     * @param inputs  the number of inputs (in all queries) used
     * @param time    the time (ms) spent until this snapshot
     */
    public StatisticsSnapshot(long tests, long inputs, long time) {
        this.tests = tests;
        this.inputs = inputs;
        this.time = time;
    }
}
