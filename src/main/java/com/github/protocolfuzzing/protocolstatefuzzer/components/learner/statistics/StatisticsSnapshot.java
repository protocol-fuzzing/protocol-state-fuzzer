package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

/**
 * Used to store a snapshot of relevant statistics at selected phases during the learning process.
 */
public class StatisticsSnapshot {
    protected long inputs;
    protected long tests;
    protected long time;

    public long getTests() {
        return tests;
    }

    public long getInputs() {
        return inputs;
    }

    public long getTime() {
        return time;
    }

    public StatisticsSnapshot(long tests, long inputs, long time) {
        this.tests = tests;
        this.inputs = inputs;
        this.time = time;
    }
}
