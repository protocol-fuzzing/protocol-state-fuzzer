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
 * An empty LearnerResult is used to indicate an error. A normal LearnerResult
 * can be converted to empty using {@link #toEmpty()} and can be checked
 * for emptiness using {@link #isEmpty()}. An empty LearnerResult can be
 * converted back to normal using {@link #toNormal()}.
 */
public class LearnerResult<I, O> {

    /** Stores the list of intermediate hypothesis. */
    protected List<StateMachine<I, O>> hypotheses;

    /** Stores the learned model. */
    protected StateMachine<I, O> learnedModel;

    /** Stores the file, in which the learned model has been outputted. */
    protected File learnedModelFile;

    /** Stores the collected statistics of the learning process. */
    protected Statistics<I, O> statistics;

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
    public LearnerResult<I, O> toEmpty() {
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
     * Returns a reference to the same instance after converting an empty
     * LearnerResult back to normal.
     * <p>
     * An already normal LearnerResult remains unaffected.
     *
     * @return  a reference to the same instance
     */
    public LearnerResult<I, O> toNormal() {
        if (isEmpty()) {
            hypotheses = new ArrayList<>();
        }
        return this;
    }

    /**
     * Adds a hypothesis to {@link #hypotheses} if this instance is not empty.
     *
     * @param hypothesis  the hypothesis to be added
     */
    public void addHypothesis(StateMachine<I, O> hypothesis) {
        if (!isEmpty()) {
            hypotheses.add(hypothesis);
        }
    }

    /**
     * Returns an unmodifiable list of non-null {@link #hypotheses} or null.
     *
     * @return  an unmodifiable list of non-null {@link #hypotheses} or null
     */
    public List<StateMachine<I, O>> getHypotheses() {
        return hypotheses == null ? null : Collections.unmodifiableList(hypotheses);
    }

    /**
     * Returns the stored value of {@link #learnedModel}.
     *
     * @return  the stored value of {@link #learnedModel}
     */
    public StateMachine<I, O> getLearnedModel() {
        return learnedModel;
    }

    /**
     * Sets the value of {@link #learnedModel}, if this instance is not empty.
     *
     * @param learnedModel  the learned model to be set
     */
    public void setLearnedModel(StateMachine<I, O> learnedModel) {
        if (!isEmpty()) {
            this.learnedModel = learnedModel;
        }
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
     * Sets the value of {@link #learnedModelFile}, if this instance is not empty.
     *
     * @param learnedModelFile  the file of the learned model to be set
     */
    public void setLearnedModelFile(File learnedModelFile) {
        if (!isEmpty()) {
            this.learnedModelFile = learnedModelFile;
        }
    }

    /**
     * Returns the stored value of {@link #statistics}.
     *
     * @return  the stored value of {@link #statistics}
     */
    public Statistics<I, O> getStatistics() {
        return statistics;
    }

    /**
     * Sets the value of {@link #statistics}, if this instance is not empty.
     *
     * @param statistics  the statistics to be set
     */
    public void setStatistics(Statistics<I, O> statistics) {
        if (!isEmpty()) {
            this.statistics = statistics;
        }
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
     * Sets the value of {@link #stateFuzzerEnabler}, if this instance is not empty.
     *
     * @param stateFuzzerEnabler  the StateFuzzerEnabler to be set
     */
    public void setStateFuzzerEnabler(StateFuzzerEnabler stateFuzzerEnabler) {
        if (!isEmpty()) {
            this.stateFuzzerEnabler = stateFuzzerEnabler;
        }
    }
}
