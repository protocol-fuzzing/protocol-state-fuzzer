package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

/**
 * The learning algorithms.
 * Inspired from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning">basic-learning</a>.
 */
public enum LearningAlgorithmName {
    /** It is the basic algorithm. */
    LSTAR,

    /** Performs much faster, but is more inaccurate and produces more intermediate hypotheses.*/
    TTT,

    /** Represents the Rivest-Schapire algorithm. */
    RS,

    /** Represents the Kearns-Vazirani algorithm. */
    KV,

    /** It is currently unsupported. */
    DHC,

    /** It is currently unsupported. */
    MP,

    /** RaLambda */
    RALAMBDA,

    /** RaStar */
    RASTAR
}
