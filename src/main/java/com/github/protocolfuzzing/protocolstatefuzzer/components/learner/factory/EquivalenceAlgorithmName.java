package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

/**
 * The equivalence testing algorithms.
 */
public enum EquivalenceAlgorithmName {
    /**
     * It is the simplest, but performs poorly on large models,
     * because the chance of hitting an erroneous long trace is very small.
     */
    RANDOM_WALK,

    /** Represents the W Method, which is smarter. */
    W_METHOD,

    /** Represents the WP Method, which is smarter. */
    WP_METHOD,

    /** Check {@link com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.RandomWpMethodEQOracle}. */
    RANDOM_WP_METHOD,

    /** Check {@link com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.SampledTestsEQOracle}. */
    SAMPLED_TESTS,

    /** Check {@link com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.WpSampledTestsEQOracle}. */
    WP_SAMPLED_TESTS,

    /** It is currently unsupported. */
    MODIFIED_W_METHOD,

    /** It is currently unsupported. */
    RANDOM_WORDS
}
