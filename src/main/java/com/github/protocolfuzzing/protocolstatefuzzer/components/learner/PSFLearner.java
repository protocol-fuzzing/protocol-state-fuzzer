package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

/**
 * Shared interface for learning algorithms.
 * TODO: more content?
 */
public interface PSFLearner<I, O, T> {
    void startLearning();
    AbstractStateMachine<I, O> getHypothesis();
    void refineHypothesis(T ce);
}
