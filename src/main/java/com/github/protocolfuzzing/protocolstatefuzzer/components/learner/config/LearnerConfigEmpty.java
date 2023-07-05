package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

/**
 * The empty learning configuration without any JCommander Parameters.
 */
public class LearnerConfigEmpty implements LearnerConfig {

    /**
     * Returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public String getAlphabetFilename() {
        return null;
    }

    /**
     * Returns {@link LearningAlgorithmName#TTT}.
     *
     * @return {{@link LearningAlgorithmName#TTT}
     */
    @Override
    public LearningAlgorithmName getLearningAlgorithm() {
        return LearningAlgorithmName.TTT;
    }

    /**
     * Returns a list with {@link EquivalenceAlgorithmName#RANDOM_WP_METHOD}.
     *
     * @return  a list with {@link EquivalenceAlgorithmName#RANDOM_WP_METHOD}
     */
    @Override
    public List<EquivalenceAlgorithmName> getEquivalenceAlgorithms() {
        return List.of(EquivalenceAlgorithmName.RANDOM_WP_METHOD);
    }

    /**
     * Returns 1.
     *
     * @return  1
     */
    @Override
    public int getMaxDepth() {
        return 1;
    }

    /**
     * Returns 5.
     *
     * @return  5
     */
    @Override
    public int getMinLength() {
        return 5;
    }

    /**
     * Returns 15.
     *
     * @return  15
     */
    @Override
    public int getMaxLength() {
        return 15;
    }

    /**
     * Returns 5.
     *
     * @return  5
     */
    @Override
    public int getRandLength() {
        return 5;
    }

    /**
     * Returns 1000.
     *
     * @return  1000
     */
    @Override
    public int getEquivQueryBound() {
        return 1000;
    }

    /**
     * Returns 1.
     *
     * @return  1
     */
    @Override
    public int getRunsPerMembershipQuery() {
        return 1;
    }

    /**
     * Returns 3.
     *
     * @return  3
     */
    @Override
    public int getMembershipQueryRetries() {
        return 3;
    }

    /** Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isLogQueries() {
        return false;
    }

    /**
     * Returns 0.0.
     *
     * @return  0.0
     */
    @Override
    public double getProbReset() {
        return 0.0;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getTestFile() {
        return null;
    }

    /**
     * Returns 0.
     *
     * @return  0
     */
    @Override
    public long getSeed() {
        return 0;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isCacheTests() {
        return false;
    }

    /**
     * Returns {@code true}.
     *
     * @return  {@code true}
     */
    @Override
    public boolean isCeSanitization() {
        return true;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isSkipNonDetTests() {
        return false;
    }

    /**
     * Returns 3.
     *
     * @return  3
     */
    @Override
    public int getCeReruns() {
        return 3;
    }

    /**
     * Returns {@code true}.
     *
     * @return  {@code true}
     */
    @Override
    public boolean isProbabilisticSanitization() {
        return true;
    }


    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public Duration getTimeLimit() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public Long getTestLimit() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public Integer getRoundLimit() {
        return null;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("LearnerConfigEmpty Non-Explicit Parameters");
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
