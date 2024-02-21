package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

/**
 * Interface regarding the learning configuration.
 */
public interface LearnerConfig extends RunDescriptionPrinter {

    /**
     * Returns the filename of the alphabet to be used for learning.
     * <p>
     * Default value: null.
     *
     * @return  the filename of the alphabet to be used for learning
     */
    default String getAlphabetFilename() {
        return null;
    }

    /**
     * Returns the algorithm that should be used for learning.
     * <p>
     * Default value: null.
     *
     * @return  the algorithm that should be used for learning
     */
    default LearningAlgorithmName getLearningAlgorithm() {
        return null;
    }

    /**
     * Returns the algorithms that should be used for equivalence testing.
     * <p>
     * Default value: [RANDOM_WP_METHOD].
     *
     * @return  the algorithms that should be used for equivalence testing
     */
    default List<EquivalenceAlgorithmName> getEquivalenceAlgorithms() {
        return List.of(EquivalenceAlgorithmName.RANDOM_WP_METHOD);
    };

    /**
     * Returns the maximal depth (W/WP Method).
     * <p>
     * Default value: 1.
     *
     * @return  the maximal depth (W/WP Method)
     */
    default int getMaxDepth() {
        return 1;
    }

    /**
     * Returns the minimum length (random words, Random WP Method).
     * <p>
     * Default value: 5.
     *
     * @return  the minimum length (random words, Random WP Method)
     */
    default int getMinLength() {
        return 5;
    }

    /**
     * Returns the maximum length (random words).
     * <p>
     * Default value: 15.
     *
     * @return  the maximum length (random words)
     */
    default int getMaxLength() {
        return 15;
    }

    /**
     * Returns the size of the random part (Random WP Method).
     * <p>
     * Default value: 5.
     *
     * @return  the size of the random part (Random WP Method)
     */
    default int getRandLength() {
        return 5;
    }

    /**
     * Returns the maximum number of queries used by some equivalence algorithms.
     * <p>
     * It is used as the 'bound' parameter in those equivalence algorithms.
     * <p>
     * Default value: 1000.
     *
     * @return  the maximum number of queries used by some equivalence algorithms
     */
    default int getEquivQueryBound() {
        return 1000;
    }

    /**
     * Returns the number of times each membership query is executed before an answer is returned.
     * <p>
     * Default value: 1.
     *
     * @return  the number of times each membership query is executed before an answer is returned
     */
    default int getRunsPerMembershipQuery() {
        return 1;
    }

    /**
     * Returns the number of times a membership query is executed in case cache inconsistency is detected.
     * <p>
     * Default value: 3.
     *
     * @return  the number of times a membership query is executed in case cache inconsistency is detected
     */
    default int getMembershipQueryRetries() {
        return 3;
    }

    /**
     * Indicates to log all membership queries to a specific file in the output directory.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if membership query logging should occur
     */
    default boolean isLogQueries() {
        return false;
    }

    /**
     * Returns the probability of stopping the execution of a test after each input.
     * <p>
     * Default value: 0.0.
     *
     * @return  the probability of stopping the execution of a test after each input
     */
    default double getProbReset() {
        return 0.0;
    }

    /**
     * Returns a file with tests (equivalence queries) to be run.
     * <p>
     * Default value: null.
     *
     * @return  a file with tests (equivalence queries) to be run or null
     */
    default String getTestFile() {
        return null;
    }

    /**
     * Returns a seed used for random value generation.
     * <p>
     * Default value: 0.
     *
     * @return  a seed used for random value generation
     */
    default long getSeed() {
        return 0L;
    }

    /**
     * Indicates whether to cache tests (equivalence queries), which increases
     * the memory footprint, but improves performance.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if tests should be cached
     */
    default boolean isCacheTests() {
        return false;
    }

    /**
     * Indicates if counterexamples (CE) sanitization should be enabled,
     * which involves re-running potential CEs ensuring they are not spurious.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if CE sanitization should be enabled
     */
    default boolean isCeSanitization() {
        return false;
    }

    /**
     * Indicates whether to log and skip tests, whose execution turned out
     * non-deterministic, rather than throwing an exception.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if non-deterministic tests should be skipped
     */
    default boolean isSkipNonDetTests() {
        return false;
    }

    /**
     * Returns the number of times a CE is re-run in order for it to be confirmed.
     * <p>
     * Default value: 3.
     *
     * @return  the number of times a CE is re-run in order for it to be confirmed
     */
    default int getCeReruns() {
        return 3;
    }

    /**
     * Indicates if probabilistic sanitization of CEs resulting in non determinism
     * should be enabled.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if probabilistic sanitization should be enabled
     */
    default boolean isProbabilisticSanitization() {
        return false;
    }

    /**
     * Returns null or a time limit on the learning experiment.
     * <p>
     * Default value: null.
     *
     * @return  null or a time limit on the learning experiment
     */
    default Duration getTimeLimit() {
        return null;
    }

    /**
     * Returns null or a test limit on the learning experiment.
     * <p>
     * Default value: null.
     *
     * @return  null or a test limit on the learning experiment
     */
    default Long getTestLimit() {
        return null;
    }

    /**
     * Returns null or a round limit on the learning experiment.
     * <p>
     * Default value: null.
     *
     * @return  null or a round limit on the learning experiment
     */
    default Integer getRoundLimit() {
        return null;
    }

    /**
     * Whether or not IOMode should be enabled.
     *
     * @return {@code true} if IOMode is enabled.
     */
    default Boolean getIOMode() {
        return true;
    }

    /**
     * The probability that a new data value is selected during a IO random walk run.
     *
     * @return Double precition floating point between 0 and 1.
     */
    default Double getProbNewDataValue() {
        return 0.1;
    }

    /**
     * The maximum number of IO random walk runs.
     *
     * @return Integer greater than zero.
     */
    default Integer getMaxRuns() {
        return 1;
    }

    /**
     * Returns the max depth of an IO random walk run.
     *
     * @return  the max depth of a run
     */
    default Integer getMaxDepthRA() {
        return 1;
    }

    /**
     * Returns if IO random walk runs should be reset.
     *
     * @return  true if runs should be reset, otherwise false
     */
    default Boolean getResetRuns() {
        return true;
    }

    /**
     * Returns if seed transitions should be done or not for IO random walks.
     *
     * @return  true if seed transitions should be done, otherwise false
     */
    default Boolean getSeedTransitions() {
        return true;
    }

    /**
     * Returns if symbols should be drawn uniformly or not for IO random walks.
     *
     * @return  true if symbols should be drawn uniformly, otherwise false.
     */
    default Boolean getDrawSymbolsUniformly() {
        return true;
    }

    @Override
    // TODO: Add RALib options
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("LearnerConfig Parameters");
        printWriter.println("Alphabet: " + getAlphabetFilename());
        printWriter.println("Learning Algorithm: " + getLearningAlgorithm());
        printWriter.println("Equivalence Algorithms: " + getEquivalenceAlgorithms());
        printWriter.println("Max Depth: " + getMaxDepth());
        printWriter.println("Min Length: " + getMinLength());
        printWriter.println("Max Length: " + getMaxLength());
        printWriter.println("Max Equivalence Queries: " + getEquivQueryBound());
        printWriter.println("Runs Per Membership Query: " + getRunsPerMembershipQuery());
        printWriter.println("Random Length: " + getRandLength());
        printWriter.println("Membership Query Retries: " + getMembershipQueryRetries());
        printWriter.println("Log Queries: " + isLogQueries());
        printWriter.println("Prob Reset: " + getProbReset());
        printWriter.println("Test File: " + getTestFile());
        printWriter.println("Seed: " + getSeed());
        printWriter.println("Cache Tests: " + isCacheTests());
        printWriter.println("Ce Sanitization: " + isCeSanitization());
        printWriter.println("Skip Non Det Tests: " + isSkipNonDetTests());
        printWriter.println("Ce Reruns: " + getCeReruns());
        printWriter.println("Probabilistic Sanitization: " + isProbabilisticSanitization());
        printWriter.println("Time Limit: " + getTimeLimit());
        printWriter.println("Test Limit: " + getTestLimit());
        printWriter.println("Round Limit: " + getRoundLimit());
    }
}
