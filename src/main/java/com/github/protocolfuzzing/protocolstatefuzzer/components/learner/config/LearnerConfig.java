package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

public class LearnerConfig implements AlphabetOptionProvider, RunDescriptionPrinter {

    @Parameter(names = "-alphabet", description = "A file defining the input alphabet. "
            + "The alphabet is used to interpret inputs from a given specification, as well as to learn. "
            + "Each input in the alphabet has a name under which it appears in the specification."
            + "The name can be changed by setting the 'name' attribute in xml format for example.")
    protected String alphabet = null;

    @Parameter(names = "-learningAlgorithm", description = "Which algorithm should be used for learning")
    protected LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.TTT;

    @Parameter(names = "-equivalenceAlgorithms", description = "Which test algorithms should be used for "
            + "equivalence testing. Expected comma-separated values of [W_METHOD, MODIFIED_W_METHOD, WP_METHOD, "
            + "RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD, SAMPLED_TESTS, WP_SAMPLED_TESTS]")
    protected List<EquivalenceAlgorithmName> equivalenceAlgorithms = List.of(EquivalenceAlgorithmName.RANDOM_WP_METHOD);

    @Parameter(names = "-depth", description = "Maximal depth (W/WP Method)")
    protected Integer maxDepth = 1;

    @Parameter(names = "-minLength", description = "Min length (random words, Random WP Method)")
    protected Integer minLength = 5;

    @Parameter(names = "-maxLength", description = "Max length (random words)")
    protected Integer maxLength = 15;

    @Parameter(names = "-randLength", description = "Size of the random part (Random WP Method)")
    protected Integer randLength = 5;

    @Parameter(names = {"-equivalenceQueryBound", "-eqvQueries"}, description = "Max number of queries used by some equivalence algorithms."
            + "It is used as the 'bound' parameter in those equivalence algorithms.")
    protected Integer equivQueryBound = 1000;

    @Parameter(names = "-memQueryRuns", description = "The number of times each membership query is executed before "
            + "an answer is returned. Setting it to more than 1 enables an multiple-run oracle "
            + "which may prevent non-determinism.")
    protected Integer runsPerMembershipQuery = 1;

    @Parameter(names = "-memQueryRetries", description = "The number of times a membership query is executed in case "
            + "cache inconsistency is detected.")
    protected Integer membershipQueryRetries = 3;

    @Parameter(names = "-logQueries", description = "If set, logs all membership queries to a specific file in the "
            + "output directory.")
    protected boolean logQueries = false;

    @Parameter(names = "-probReset", description = "Probability of stopping execution of a test after each input")
    protected Double probReset = 0.0;

    @Parameter(names = "-testFile", description = "A file with tests to be run.")
    protected String testFile = null;

    @Parameter(names = "-seed", description = "Seed used for random value generation.")
    protected Long seed = 0L;

    @Parameter(names = "-cacheTests", description = "Cache tests, which increases the memory footprint, "
            + "but improves performance. It also renders useless most forms of non-determinism sanitization")
    protected boolean cacheTests = false;

    @Parameter(names = "-ceSanitizationDisable", description = "Disables counterexamples (CE) sanitization, "
            + "which involves re-running potential CE's ensuring they are not spurious")
    protected boolean ceSanitizationDisable = false;

    @Parameter(names = "-skipNonDetTests", description = "Rather than throw an exception, logs and skips tests, "
            + "whose execution turned out non-deterministic")
    protected boolean skipNonDetTests = false;

    @Parameter(names = "-ceReruns", description = "Represents the number of times a CE is re-run in order for it to "
            + "be confirmed")
    protected Integer ceReruns = 3;

    @Parameter(names = "-probabilisticSanitizationDisable", description = "Disables probabilistic sanitization of "
            + "the CEs resulting in non determinism")
    protected boolean probabilisticSanitizationDisable = false;

    @Parameter(names = "-timeLimit", description = "If set, imposes a time limit on the learning experiment. "
            + "Once this time elapses, learning is stopped and statistics for the incomplete learning run are published "
            + "The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days considered to be "
            + "exactly 24 hours. See java.time.Duration#parse(java.lang.CharSequence) for specific details on format.",
            converter = DurationConverter.class)
    protected Duration timeLimit = null;

    @Parameter(names = "-testLimit", description = "If set, imposes a test limit on the learning experiment. Once the "
            + "the number of tests has reached this limit, learning is stopped and statistics for the incomplete "
            + "learning run are published")
    protected Long testLimit = null;

    @Parameter(names = "-roundLimit", description = "If set, limits the number of hypothesis construction rounds "
            + "and with that, the number of hypotheses generated. Once the limit is reached, learning is stopped and "
            + "statistics for the incomplete learning run are published.")
    protected Integer roundLimit = null;

    public String getAlphabet() {
        return alphabet;
    }

    public LearningAlgorithmName getLearningAlgorithm() {
        return learningAlgorithm;
    }

    public List<EquivalenceAlgorithmName> getEquivalenceAlgorithms() {
        return equivalenceAlgorithms;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getRandLength() {
        return randLength;
    }

    public int getEquivQueryBound() {
        return equivQueryBound;
    }

    public int getRunsPerMembershipQuery() {
        return runsPerMembershipQuery;
    }

    public int getMembershipQueryRetries() {
        return membershipQueryRetries;
    }

    public boolean isLogQueries() {
        return logQueries;
    }

    public double getProbReset() {
        return probReset;
    }

    public String getTestFile() {
        return testFile;
    }

    public long getSeed() {
        return seed;
    }

    public boolean isCacheTests() {
        return cacheTests;
    }

    public boolean isCeSanitization() {
        return !ceSanitizationDisable;
    }

    public boolean isSkipNonDetTests() {
        return skipNonDetTests;
    }

    public int getCeReruns() {
        return ceReruns;
    }

    public boolean isProbabilisticSanitization() {
        return !probabilisticSanitizationDisable;
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    public Long getTestLimit() {
        return testLimit;
    }

    public Integer getRoundLimit() {
        return roundLimit;
    }

    public void printRunDescription(PrintWriter printWriter) {
        printWriter.println("LearnerConfig Parameters");
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
