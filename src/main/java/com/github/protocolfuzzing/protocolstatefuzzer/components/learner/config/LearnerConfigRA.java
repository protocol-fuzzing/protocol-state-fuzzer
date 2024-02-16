package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.Parameter;

import java.io.PrintWriter;

/**
 * The standard learning configuration for Register Automata.
 */
public class LearnerConfigRA extends LearnerConfigStandard {

    /**
     * Stores the JCommander Parameter -ioMode
     * <p>
     * Whether or not to use IO mode.
     * <p>
     * Default value: true.
     */
    // TODO: Explain what IO Mode is
    @Parameter(names = "-ioMode", description = "Whether or not to use IO mode, by default set to true.")
    protected Boolean ioMode = true;

    /**
     * Returns the stored value of {@link #ioMode}.
     *
     * @return  the stored value of {@link #ioMode}
     */
    public Boolean getIOMode() {
        return ioMode;
    }

    /**
     * Stores the JCommander Parameter -probNewDataValue
     * <p>
     * The probability of the Equivalence Oracle choosing a new data value
     * <p>
     * Default value: 0.8.
     */
    // TODO: Is the description correct?
    @Parameter(names = "-probNewDataValue", description = "The probability of some RA equivalence algorithms to choose a new data value")
    protected Double probNewDataValue = 0.8;

    /**
     * Returns the stored value of {@link #ioMode}.
     *
     * @return  the stored value of {@link #ioMode}
     */
    public Double getProbNewDataValue() {
        return probNewDataValue;
    }

    /**
     * Stores the JCommander Parameter -maxRuns
     * <p>
     * The maximum number of runs for some RA equivalence algorithms
     * <p>
     * Default value: 10000.
     */
    @Parameter(names = "-maxRuns", description = "The maximum number of runs for some RA equivalence algorithms")
    protected Integer maxRuns = 10000;

    /**
     * Returns the stored value of {@link #maxRuns}.
     *
     * @return  the stored value of {@link #maxRuns}
     */
    public Integer getMaxRuns() {
        return maxRuns;
    }

    /**
     * Stores the JCommander Parameter -maxDepthRA
     * <p>
     * The maximum depth for some RA equivalence algorithms
     * <p>
     * Default value: 10.
     */
    @Parameter(names = "-maxDepthRA", description = "The maximum depth for some RA equivalence algorithms")
    protected Integer maxDepthRA = 10;

    /**
     * Returns the stored value of {@link #maxDepthRA}.
     *
     * @return  the stored value of {@link #maxDepthRA}
     */
    public Integer getMaxDepthRA() {
        return maxDepthRA;
    }

    /**
     * Stores the JCommander Parameter -resetRuns
     * <p>
     * Whether or not to reset runs.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-resetRuns", description = "Whether or not to reset runs.")
    protected Boolean resetRuns = false;

    /**
     * Returns the stored value of {@link #resetRuns}.
     *
     * @return  the stored value of {@link #resetRuns}
     */
    public Boolean getResetRuns() {
        return resetRuns;
    }

    /**
     * Stores the JCommander Parameter -SeedTransitions
     * <p>
     * Whether or not transitions should be seeded
     * <p>
     * Default value: false.
     */
    // TODO: Explain what seeding transitions means
    @Parameter(names = "-seedTransitions", description = "Whether or not transitions should be seeded.")
    protected Boolean seedTransitions = false;

    /**
     * Returns the stored value of {@link SeedTransitions}.
     *
     * @return  the stored value of {@link SeedTransitions}
     */
    public Boolean getSeedTransitions() {
        return seedTransitions;
    }

        /**
     * Stores the JCommander Parameter -drawSymbolsUniformly
     * <p>
     * Whether or not symbols should be drawn uniformly.
     * <p>
     * Default value: false.
     */
    // TODO: Explain what drawing uniformly means
    @Parameter(names = "-drawSymbolsUniformly", description = "Whether or not symbols should be drawn uniformly.")
    protected Boolean drawSymbolsUniformly = false;

    /**
     * Returns the stored value of {@link #drawSymbolsUniformly}.
     *
     * @return  the stored value of {@link #drawSymbolsUniformly}
     */
    public Boolean getDrawSymbolsUniformly() {
        return drawSymbolsUniformly;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("LearnerConfigStandard Parameters");
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
