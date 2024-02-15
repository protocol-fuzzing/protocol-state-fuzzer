package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

/**
 * Statistics that concern a specific hypothesis identified by an index number.
 *
 * @param <ID>  the type of input domain
 * @param <OD>  the type of output domain
 * @param <CE>  the type of output domain
 */
public class HypothesisStatistics<ID, OD, CE> {

    /** The hypothesis for which the statistics are stored. */
    protected StateMachineWrapper<ID, OD> hypothesis;

    /** The index number used to identify the hypothesis among others. */
    protected int index;

    /** Statistics Snapshot of the hypothesis. */
    protected StatisticsSnapshot snapshot;

    /** The counterexample found for the hypothesis. */
    protected CE counterexample;

    /** Statistics Snapshot of the counterexample. */
    protected StatisticsSnapshot counterexampleSnapshot;


    /**
     * Returns the stored {@link #hypothesis}.
     *
     * @return  the stored {@link #hypothesis}
     */
    public StateMachineWrapper<ID, OD> getHypothesis() {
        return hypothesis;
    }

    /**
     * Sets the {@link #hypothesis}.
     *
     * @param hypothesis  the hypothesis to be set
     */
    public void setHypothesis(StateMachineWrapper<ID, OD> hypothesis) {
        this.hypothesis = hypothesis;
    }

    /**
     * Returns the stored {@link #index}.
     *
     * @return  the stored {@link #index}
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the {@link #index}.
     *
     * @param index  the index to be set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the stored {@link #snapshot}.
     *
     * @return  the stored {@link #snapshot}
     */
    public StatisticsSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the {@link #snapshot}.
     *
     * @param snapshot  the snapshot to be set
     */
    public void setSnapshot(StatisticsSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Returns the stored {@link #counterexample}.
     *
     * @return  the stored {@link #counterexample}
     */
    public CE getCounterexample() {
        return counterexample;
    }

    /**
     * Sets the {@link #counterexample}.
     *
     * @param counterexample  the counterexample to be set
     */
    public void setCounterexample(CE counterexample) {
        this.counterexample = counterexample;
    }

    /**
     * Returns the stored {@link #counterexampleSnapshot}.
     *
     * @return  the stored {@link #counterexampleSnapshot}
     */
    public StatisticsSnapshot getCounterexampleSnapshot() {
        return counterexampleSnapshot;
    }

    /**
     * Sets the {@link #counterexampleSnapshot}.
     *
     * @param counterexampleSnapshot  the counterexample snapshot to be set
     */
    public void setCounterexampleSnapshot(StatisticsSnapshot counterexampleSnapshot) {
        this.counterexampleSnapshot = counterexampleSnapshot;
    }
}
