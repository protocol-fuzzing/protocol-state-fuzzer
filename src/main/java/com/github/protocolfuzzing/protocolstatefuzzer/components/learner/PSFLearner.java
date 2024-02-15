package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StateMachineWrapper;
/**
 * Shared interface for learning algorithms.
 * TODO: more content?
 */
public interface PSFLearner<I, O, T> {
    void startLearning();
    StateMachineWrapper<I, O> getHypothesis();
    void refineHypothesis(T ce);
}
