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
     * Whether or not to use IO mode. This decides if the learner will treat the SUL
     * as an I/O automata (such as Mealy machines) or an acceptor (such as DFAs).
     * <p>
     * Default value: true.
     */
    @Parameter(names = "-disableIOMode", description = "Whether or not to use IO mode, IOMode is used by default. WARNING: Disabling IO-mode will learn the system as an acceptor, unsupported.")
    protected Boolean disableIOMode = false;

    /**
     * Returns the stored value of {@link #ioMode}.
     *
     * @return the stored value of {@link #ioMode}
     */
    @Override
    public Boolean getDisableIOMode() {
        return disableIOMode;
    }

    /**
     * Stores the JCommander Parameter -probNewDataValue
     * <p>
     * The probability of the Equivalence Oracle choosing a new data value
     * <p>
     * Default value: 0.8.
     */
    @Parameter(names = "-probNewDataValue", description = "The probability of some RA equivalence algorithms to choose a new data value")
    protected Double probNewDataValue = 0.8;

    /**
     * Returns the stored value of {@link #ioMode}.
     *
     * @return the stored value of {@link #ioMode}
     */
    @Override
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
     * @return the stored value of {@link #maxRuns}
     */
    @Override
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
     * @return the stored value of {@link #maxDepthRA}
     */
    @Override
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
     * @return the stored value of {@link #resetRuns}
     */
    @Override
    public Boolean getResetRuns() {
        return resetRuns;
    }

    /**
     * Stores the JCommander Parameter -SeedTransitions
     * <p>
     * Whether or not transitions should be seeded. If set to true then the
     * equivalence oracle picks a random starting location in the hypothesis and
     * generates a random trace from that location, otherwise only a random location
     * is generated.
     * Setting this to true can speed up the process of finding counter examples.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-seedTransitions", description = "Whether or not transitions should be seeded.")
    protected Boolean seedTransitions = false;

    /**
     * Returns the stored value of {@link #seedTransitions}.
     *
     * @return the stored value of {@link #seedTransitions}
     */
    @Override
    public Boolean getSeedTransitions() {
        return seedTransitions;
    }

    /**
     * Stores the JCommander Parameter -drawSymbolsUniformly
     * <p>
     * Whether or not symbols should be drawn uniformly. This affects how the
     * equivalence oracle generates the random trace where false means that the next
     * action is picked at random while true means it is chosen by a weighted
     * random.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-drawSymbolsUniformly", description = "Whether or not symbols should be drawn uniformly.")
    protected Boolean drawSymbolsUniformly = false;

    /**
     * Returns the stored value of {@link #drawSymbolsUniformly}.
     *
     * @return the stored value of {@link #drawSymbolsUniformly}
     */
    @Override
    public Boolean getDrawSymbolsUniformly() {
        return drawSymbolsUniformly;
    }

    @Override
    // TODO: Add RALib options
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
        printWriter.println("IOMode: " + !getDisableIOMode());
        printWriter.println("Probability of Choosing a New DataValue: " + getProbNewDataValue());
        printWriter.println("Max Runs: " + getMaxRuns());
        printWriter.println("Max Depth for Register Automata: " + getMaxDepthRA());
        printWriter.println("Reset Runs: " + getResetRuns());
        printWriter.println("Seed transitions: " + getSeedTransitions());
        printWriter.println("Draw symbols uniformly: " + getDrawSymbolsUniformly());
    }
}
