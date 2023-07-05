package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

import java.time.Duration;
import java.util.List;

/**
 * Interface regarding the learning configuration.
 */
public interface LearnerConfig extends AlphabetProvider, RunDescriptionPrinter {

    /**
     * Returns the algorithm that should be used for learning.
     *
     * @return  the algorithm that should be used for learning
     */
    LearningAlgorithmName getLearningAlgorithm();

    /**
     * Returns the equivalence algorithms that should be used for equivalence testing.
     *
     * @return  the equivalence algorithms that should be used for equivalence testing
     */
    List<EquivalenceAlgorithmName> getEquivalenceAlgorithms();

    /**
     * Returns the maximal depth (W/WP Method).
     *
     * @return  the maximal depth (W/WP Method)
     */
    int getMaxDepth();

    /**
     * Returns the minimum length (random words, Random WP Method).
     *
     * @return  the minimum length (random words, Random WP Method)
     */
    int getMinLength();

    /**
     * Returns the maximum length (random words).
     *
     * @return  the maximum length (random words)
     */
    int getMaxLength();

    /**
     * Returns the size of the random part (Random WP Method).
     *
     * @return  the size of the random part (Random WP Method)
     */
    int getRandLength();

    /**
     * Returns the maximum number of queries used by some equivalence algorithms.
     * <p>
     * It is used as the 'bound' parameter in those equivalence algorithms.
     *
     * @return  the maximum number of queries used by some equivalence algorithms
     */
    int getEquivQueryBound();

    /**
     * Returns the number of times each membership query is executed before an answer is returned.
     *
     * @return  the number of times each membership query is executed before an answer is returned
     */
    int getRunsPerMembershipQuery();

    /**
     * Returns the number of times a membership query is executed in case cache inconsistency is detected.
     *
     * @return  the number of times a membership query is executed in case cache inconsistency is detected
     */
    int getMembershipQueryRetries();

    /**
     * Indicates to log all membership queries to a specific file in the output directory.
     *
     * @return  {@code true} if membership query logging should occur
     */
    boolean isLogQueries();

    /**
     * Returns the probability of stopping the execution of a test after each input.
     *
     * @return  the probability of stopping the execution of a test after each input
     */
    double getProbReset();

    /**
     * Returns a file with tests (equivalence queries) to be run.
     *
     * @return  a file with tests (equivalence queries) to be run or null
     */
    String getTestFile();

    /**
     * Returns a seed used for random value generation.
     *
     * @return  a seed used for random value generation
     */
    long getSeed();

    /**
     * Indicates whether to cache tests (equivalence queries), which increases
     * the memory footprint, but improves performance.
     *
     * @return  {@code true} if tests should be cached
     */
    boolean isCacheTests();

    /**
     * Indicates if counterexamples (CE) sanitization should be enabled,
     * which involves re-running potential CEs ensuring they are not spurious.
     *
     * @return  {@code true} if CE sanitization should be enabled
     */
    boolean isCeSanitization();

    /**
     * Indicates whether to log and skip tests, whose execution turned out
     * non-deterministic, rather than throwing an exception.
     *
     * @return  {@code true} if non-deterministic tests should be skipped
     */
    boolean isSkipNonDetTests();

    /**
     * Returns the number of times a CE is re-run in order for it to be confirmed.
     *
     * @return  the number of times a CE is re-run in order for it to be confirmed
     */
    int getCeReruns();

    /**
     * Indicates if probabilistic sanitization of CEs resulting in non determinism
     * should be enabled.
     *
     * @return  {@code true} if probabilistic sanitization should be enabled
     */
    boolean isProbabilisticSanitization();

    /**
     * Returns null or a time limit on the learning experiment.
     *
     * @return  null or a time limit on the learning experiment
     */
    Duration getTimeLimit();

    /**
     * Returns null or a test limit on the learning experiment.
     *
     * @return  null or a test limit on the learning experiment
     */
    Long getTestLimit();

    /**
     * Returns null or a round limit on the learning experiment.
     *
     * @return  null or a round limit on the learning experiment
     */
    Integer getRoundLimit();
}
