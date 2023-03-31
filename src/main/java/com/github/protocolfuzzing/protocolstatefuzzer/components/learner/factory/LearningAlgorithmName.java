package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

/**
 * The learning algorithms, taken from Ramon Jansen's basic-learning repo.
 * LStar is the basic algorithm, TTT performs much faster but is a bit more
 * inaccurate and produces more intermediate hypotheses, so test well.
 */
public enum LearningAlgorithmName {
    LSTAR,
    DHC,
    KV,
    TTT,
    MP,
    RS
}
