package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.Statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnerResult {

    public final List<StateMachine> hypotheses;
    public StateMachine learnedModel;
    public Statistics statistics;
    public File learnedModelFile;

    public LearnerResult() {
        this.hypotheses = new ArrayList<>();
    }

    public File getLearnedModelFile() {
        return learnedModelFile;
    }

    public void setLearnedModelFile(File learnedModelFile) {
        this.learnedModelFile = learnedModelFile;
    }

    public StateMachine getLearnedModel() {
        return learnedModel;
    }

    public void setLearnedModel(StateMachine learnedModel) {
        this.learnedModel = learnedModel;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public void addHypothesis(StateMachine hypothesis) {
        hypotheses.add(hypothesis);
    }

    public List<StateMachine> getHypotheses() {
        return Collections.unmodifiableList(hypotheses);
    }
}
