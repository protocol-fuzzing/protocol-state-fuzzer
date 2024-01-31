package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.query.DefaultQuery;

/**
 * Shared interface for learning algorithms.
 * TODO: more content?
 */
public interface PSFLearner<T> {
    void startLearning();
    AbstractStateMachine getHypothesis();
    
    void refineHypothesis(T ce);
}
