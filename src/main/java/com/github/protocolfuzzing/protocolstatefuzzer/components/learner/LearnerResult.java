package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.Statistics;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to store information about the learning process.
 * <p>
 * An 'empty' LearnerResult is used to indicate an error. A normal LearnerResult
 * can be converted to 'empty' using {@link #toEmpty()} and can be checked
 * for emptiness using {@link #isEmpty()}.
 */
public class LearnerResult {

    /** Stores the list of intermediate hypothesis. */
    protected List<StateMachine> hypotheses;

    /** Stores the learned model. */
    protected StateMachine learnedModel;

    /** Stores the file, in which the learned model has been outputted. */
    protected File learnedModelFile;

    /** Stores the collected statistics of the learning process. */
    protected Statistics statistics;

    /** Stores the StateFuzzerEnabler used in the learning process. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /**
     * Constructs a new instance, initializing parameters to null except for the
     * {@link #hypotheses}.
     */
    public LearnerResult() {
        hypotheses = new ArrayList<>();
        learnedModel = null;
        learnedModelFile = null;
        statistics = null;
        stateFuzzerEnabler = null;
    }

    /**
     * Returns a reference to the same instance after initializing every parameter to null.
     * <p>
     * The {@code get} methods will return null after this.
     *
     * @return  a reference to the same instance
     */
    public LearnerResult toEmpty() {
        hypotheses = null;
        learnedModel = null;
        learnedModelFile = null;
        statistics = null;
        stateFuzzerEnabler = null;
        return this;
    }

    /**
     * Checks if this instance is empty.
     *
     * @return  {@code true} if this instance is empty
     */
    public boolean isEmpty() {
        return hypotheses == null
            && learnedModel == null
            && learnedModelFile == null
            && statistics == null
            && stateFuzzerEnabler == null;
    }

    /**
     * Adds a hypothesis to {@link #hypotheses} if the underlying list is not null.
     *
     * @param hypothesis  the hypothesis to be added
     */
    public void addHypothesis(StateMachine hypothesis) {
        if (hypotheses != null) {
            hypotheses.add(hypothesis);
        }
    }

    /**
     * Returns an unmodifiable list of non-null {@link #hypotheses} or null.
     *
     * @return  an unmodifiable list of non-null {@link #hypotheses} or null
     */
    public List<StateMachine> getHypotheses() {
        return hypotheses == null ? null : Collections.unmodifiableList(hypotheses);
    }

    /**
     * Returns the stored value of {@link #learnedModel}.
     *
     * @return  the stored value of {@link #learnedModel}
     */
    public StateMachine getLearnedModel() {
        return learnedModel;
    }

    /**
     * Sets the value of {@link #learnedModel}.
     *
     * @param learnedModel  the learned model to be set
     */
    public void setLearnedModel(StateMachine learnedModel) {
        this.learnedModel = learnedModel;
    }

    /**
     * Returns the stored value of {@link #learnedModelFile}.
     *
     * @return  the stored value of {@link #learnedModelFile}
     */
    public File getLearnedModelFile() {
        return learnedModelFile;
    }

    /**
     * Sets the value of {@link #learnedModelFile}.
     *
     * @param learnedModelFile  the file of the learned model to be set
     */
    public void setLearnedModelFile(File learnedModelFile) {
        this.learnedModelFile = learnedModelFile;
    }

    /**
     * Returns the stored value of {@link #statistics}.
     *
     * @return  the stored value of {@link #statistics}
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the value of {@link #statistics}.
     *
     * @param statistics  the statistics to be set
     */
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the stored value of {@link #stateFuzzerEnabler}.
     *
     * @return  the stored value of {@link #stateFuzzerEnabler}
     */
    public StateFuzzerEnabler getStateFuzzerEnabler() {
        return stateFuzzerEnabler;
    }

    /**
     * Sets the value of {@link #stateFuzzerEnabler}.
     *
     * @param stateFuzzerEnabler  the StateFuzzerEnabler to be set
     */
    public void setStateFuzzerEnabler(StateFuzzerEnabler stateFuzzerEnabler) {
        this.stateFuzzerEnabler = stateFuzzerEnabler;
    }

}
