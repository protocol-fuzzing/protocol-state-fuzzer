package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.api.query.DefaultQuery;

public class HypothesisStatistics {
    protected StatisticsSnapshot snapshot;
    protected DefaultQuery<?, ?> counterexample;
    protected int states;
    protected int index;
    protected StatisticsSnapshot counterexampleSnapshot;


    public int getStates() {
        return states;
    }
    public void setStates(int states) {
        this.states = states;
    }
    public StatisticsSnapshot getCounterexampleSnapshot() {
        return counterexampleSnapshot;
    }
    public void setCounterexampleSnapshot(StatisticsSnapshot counterexampleSnapshot) {
        this.counterexampleSnapshot = counterexampleSnapshot;
    }
    public StatisticsSnapshot getSnapshot() {
        return snapshot;
    }
    public void setSnapshot(StatisticsSnapshot snapshot) {
        this.snapshot = snapshot;
    }
    public DefaultQuery<?, ?> getCounterexample() {
        return counterexample;
    }
    public void setCounterexample(DefaultQuery<?, ?> counterexample) {
        this.counterexample = counterexample;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

}
