package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StateMachineWrapper;
/**
 * Shared interface for learning algorithms.
 * TODO: more content?
 */
public interface PSFLearner<I, O, ID, OD, CE, W extends StateMachineWrapper<ID, OD>> {
    void startLearning();
    W getHypothesis();
    void refineHypothesis(CE ce);
}
