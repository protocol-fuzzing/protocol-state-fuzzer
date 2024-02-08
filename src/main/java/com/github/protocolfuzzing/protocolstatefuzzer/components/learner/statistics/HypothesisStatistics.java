package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.StateMachine;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;

/**
 * Statistics that concern a specific hypothesis identified by an index number.
 */
public class HypothesisStatistics<I, O> {

    /** The hypothesis for which the statistics are stored. */
    protected StateMachine<I, O> hypothesis;

    /** The index number used to identify the hypothesis among others. */
    protected int index;

    /** Statistics Snapshot of the hypothesis. */
    protected StatisticsSnapshot snapshot;

    /** The counterexample found for the hypothesis. */
    protected DefaultQuery<I, Word<O>> counterexample;

    /** Statistics Snapshot of the counterexample. */
    protected StatisticsSnapshot counterexampleSnapshot;


    /**
     * Returns the stored {@link #hypothesis}.
     *
     * @return  the stored {@link #hypothesis}
     */
    public StateMachine<I, O> getHypothesis() {
        return hypothesis;
    }

    /**
     * Sets the {@link #hypothesis}.
     *
     * @param hypothesis  the hypothesis to be set
     */
    public void setHypothesis(StateMachine<I, O> hypothesis) {
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
    public DefaultQuery<I, Word<O>> getCounterexample() {
        return counterexample;
    }

    /**
     * Sets the {@link #counterexample}.
     *
     * @param counterexample  the counterexample to be set
     */
    public void setCounterexample(DefaultQuery<I, Word<O>> counterexample) {
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
