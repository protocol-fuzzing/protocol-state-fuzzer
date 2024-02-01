package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

/**
 * Shared interface for learning algorithms.
 * TODO: more content?
 */
public interface PSFLearner<T> {
    void startLearning();
    AbstractStateMachine getHypothesis();
    void refineHypothesis(T ce);
}
