package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;

import java.time.Duration;
import java.util.List;

/**
 * The standard learning configuration.
 */
public class LearnerConfigStandard implements LearnerConfig {

    /**
     * Stores the JCommander Parameter -alphabet.
     * <p>
     * A file defining the input alphabet. If it is not provided then the default
     * alphabet in resources would be used. The alphabet is used to interpret
     * inputs from a given specification, as well as to learn. Each input in
     * the alphabet has a name under which it appears in the specification.
     * In XML format, for example, the name is specified using the 'name' attribute.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-alphabet", description = "A file defining the input alphabet. "
            + "If it is not provided then the default alphabet in resources would be used. "
            + "The alphabet is used to interpret inputs from a given specification, as well as to learn. "
            + "Each input in the alphabet has a name under which it appears in the specification. "
            + "In XML format, for example, the name is specified using the 'name' attribute.")
    protected String alphabetFilename = null;

    /**
     * Stores the JCommander Parameter -learningAlgorithm.
     * <p>
     * Which algorithm should be used for learning.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-learningAlgorithm", description = "Which algorithm should be used for learning")
    protected LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.TTT;

    /**
     * Stores the JCommander Parameter -equivalenceAlgorithms.
     * <p>
     * Which test algorithms should be used for equivalence testing.
     * Expected comma-separated values of
     * [W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK,
     * RANDOM_WP_METHOD, SAMPLED_TESTS, WP_SAMPLED_TESTS].
     * <p>
     * Default value: [RANDOM_WP_METHOD].
     */
    @Parameter(names = "-equivalenceAlgorithms", description = "Which test algorithms should be used for "
            + "equivalence testing. Expected comma-separated values of: W_METHOD, MODIFIED_W_METHOD, WP_METHOD, "
            + "RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD, SAMPLED_TESTS, WP_SAMPLED_TESTS. "
            + "Do not leave any whitespace in between or after the final value")
    protected List<EquivalenceAlgorithmName> equivalenceAlgorithms = List.of(EquivalenceAlgorithmName.RANDOM_WP_METHOD);

    /**
     * Stores the JCommander Parameter -depth.
     * <p>
     * Maximal depth (W/WP Method).
     * <p>
     * Default value: 1.
     */
    @Parameter(names = "-depth", description = "Maximal depth (W/WP Method)")
    protected Integer maxDepth = 1;

    /**
     * Stores the JCommander Parameter -minLength.
     * <p>
     * Min length (random words, Random WP Method).
     * <p>
     * Default value: 5.
     */
    @Parameter(names = "-minLength", description = "Min length (random words, Random WP Method)")
    protected Integer minLength = 5;

    /**
     * Stores the JCommander Parameter -maxLength.
     * <p>
     * Max length (random words).
     * <p>
     * Default value: 15.
     */
    @Parameter(names = "-maxLength", description = "Max length (random words)")
    protected Integer maxLength = 15;

    /**
     * Stores the JCommander Parameter -randLength.
     * <p>
     * Size of the random part (Random WP Method).
     * <p>
     * Default value: 5.
     */
    @Parameter(names = "-randLength", description = "Size of the random part (Random WP Method)")
    protected Integer randLength = 5;

    /**
     * Stores the JCommander Parameter -equivalenceQueryBound, -eqvQueries.
     * <p>
     * Max number of queries used by some equivalence algorithms.
     * It is used as the 'bound' parameter in those equivalence algorithms.
     * <p>
     * Default value: 1000.
     */
    @Parameter(names = {"-equivalenceQueryBound", "-eqvQueries"}, description = "Max number of queries used by some equivalence algorithms. "
            + "It is used as the 'bound' parameter in those equivalence algorithms.")
    protected Integer equivQueryBound = 1000;

    /**
     * Stores the JCommander Parameter -memQueryRuns.
     * <p>
     * The number of times each membership query is executed before an answer is returned.
     * Setting it to more than 1 enables a multiple-run oracle which can be used to resolve non-determinism.
     * <p>
     * Default value: 1.
     */
    @Parameter(names = "-memQueryRuns", description = "The number of times each membership query is executed before "
            + "an answer is returned. Setting it to more than 1 enables a multiple-run oracle "
            + "which can be used to resolve non-determinism.")
    protected Integer runsPerMembershipQuery = 1;

    /**
     * Stores the JCommander Parameter -memQueryRetries.
     * <p>
     * The number of times a membership query is executed in case cache inconsistency is detected.
     * <p>
     * Default value: 3.
     */
    @Parameter(names = "-memQueryRetries", description = "The number of times a membership query is executed in case "
            + "cache inconsistency is detected.")
    protected Integer membershipQueryRetries = 3;

    /**
     * Stores the JCommander Parameter -logQueries.
     * <p>
     * If set, logs all membership queries to a specific file in the output directory.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-logQueries", description = "If set, logs all membership queries to a specific file in the "
            + "output directory.")
    protected boolean logQueries = false;

    /**
     * Stores the JCommander Parameter -probReset.
     * <p>
     * Probability of stopping the execution of a test after each input.
     * <p>
     * Default value: 0.0.
     */
    @Parameter(names = "-probReset", description = "Probability of stopping the execution of a test after each input")
    protected Double probReset = 0.0;

    /**
     * Stores the JCommander Parameter -testFile.
     * <p>
     * A file with tests (equivalence queries) to be run.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-testFile", description = "A file with tests to be run.")
    protected String testFile = null;

    /**
     * Stores the JCommander Parameter -seed.
     * <p>
     * Seed used for random value generation.
     * <p>
     * Default value: 0.
     */
    @Parameter(names = "-seed", description = "Seed used for random value generation.")
    protected Long seed = 0L;

    /**
     * Stores the JCommander Parameter -cacheTests.
     * <p>
     * Cache tests (equivalence queries), which increases the memory footprint,
     * but improves performance. It also renders useless most forms of
     * non-determinism sanitization.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-cacheTests", description = "Cache tests, which increases the memory footprint, "
            + "but improves performance. It also renders useless most forms of non-determinism sanitization")
    protected boolean cacheTests = false;

    /**
     * Stores the JCommander Parameter -ceSanitizationDisable.
     * <p>
     * Disables counterexamples (CE) sanitization, which involves re-running
     * potential CEs ensuring they are not spurious.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-ceSanitizationDisable", description = "Disables counterexamples (CE) sanitization, "
            + "which involves re-running potential CE's ensuring they are not spurious")
    protected boolean ceSanitizationDisable = false;

    /**
     * Stores the JCommander Parameter -skipNonDetTests.
     * <p>
     * Rather than throwing an exception, logs and skips tests, whose execution turned out non-deterministic.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-skipNonDetTests", description = "Rather than throw an exception, logs and skips tests, "
            + "whose execution turned out non-deterministic")
    protected boolean skipNonDetTests = false;

    /**
     * Stores the JCommander Parameter -ceReruns.
     * <p>
     * Represents the number of times a CE is re-run in order for it to be confirmed.
     * <p>
     * Default value: 3.
     */
    @Parameter(names = "-ceReruns", description = "Represents the number of times a CE is re-run in order for it to "
            + "be confirmed")
    protected Integer ceReruns = 3;

    /**
     * Stores the JCommander Parameter -probabilisticSanitizationDisable.
     * <p>
     * Disables probabilistic sanitization of CEs resulting in non-determinism.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-probabilisticSanitizationDisable", description = "Disables probabilistic sanitization of "
            + "CEs resulting in non-determinism")
    protected boolean probabilisticSanitizationDisable = false;

    /**
     * Stores the JCommander Parameter -timeLimit.
     * <p>
     * If set, imposes a time limit on the learning experiment. Once this time elapses,
     * learning is stopped and statistics for the incomplete learning run are published.
     * The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS
     * with days considered to be exactly 24 hours.
     * <p>
     * Default value: null.
     *
     * @see Duration#parse(CharSequence)
     */
    @Parameter(names = "-timeLimit", description = "If set, imposes a time limit on the learning experiment. "
            + "Once this time elapses, learning is stopped and statistics for the incomplete learning run are published "
            + "The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days considered to be "
            + "exactly 24 hours. See java.time.Duration#parse(java.lang.CharSequence) for specific details on format.",
            converter = DurationConverter.class)
    protected Duration timeLimit = null;

    /**
     * Stores the JCommander Parameter -testLimit.
     * <p>
     * If set, imposes a test limit on the learning experiment. Once the number of
     * tests has reached this limit, learning is stopped and statistics for the
     * incomplete learning run are published.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-testLimit", description = "If set, imposes a test limit on the learning experiment. "
            + "Once the number of tests has reached this limit, learning is stopped and statistics for the incomplete "
            + "learning run are published")
    protected Long testLimit = null;

    /**
     * Stores the JCommander Parameter -roundLimit.
     * <p>
     * If set, limits the number of hypothesis construction rounds and with that,
     * the number of hypotheses generated. Once the limit is reached, learning is
     * stopped and statistics for the incomplete learning run are published.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-roundLimit", description = "If set, limits the number of hypothesis construction rounds "
            + "and with that, the number of hypotheses generated. Once the limit is reached, learning is stopped and "
            + "statistics for the incomplete learning run are published.")
    protected Integer roundLimit = null;

    /**
     * Stores the JCommander Parameter -equivalenceThreadCount, -eqvThreads.
     * <p>
     * The number of threads to be used for the SULs.
     * <p>
     * Default value: 1.
     */
    @Parameter(names = {"-equivalenceThreadCount", "-eqvThreads"}, description = "The number of threads to parallel RandomWpMethodEQOracle (we only support this method right now)")
    protected Integer equivalenceThreadCount = 1;

    @Override
    public String getAlphabetFilename() {
        return alphabetFilename;
    }

    /**
     * Returns the stored value of {@link #learningAlgorithm}.
     *
     * @return  the stored value of {@link #learningAlgorithm}
     */
    @Override
    public LearningAlgorithmName getLearningAlgorithm() {
        return learningAlgorithm;
    }

    /**
     * Returns the stored value of {@link #equivalenceAlgorithms}.
     *
     * @return  the stored value of {@link #equivalenceAlgorithms}
     */
    @Override
    public List<EquivalenceAlgorithmName> getEquivalenceAlgorithms() {
        return equivalenceAlgorithms;
    }

    /**
     * Returns the stored value of {@link #maxDepth}.
     *
     * @return  the stored value of {@link #maxDepth}
     */
    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Returns the stored value of {@link #minLength}.
     *
     * @return  the stored value of {@link #minLength}
     */
    @Override
    public int getMinLength() {
        return minLength;
    }

    /**
     * Returns the stored value of {@link #maxLength}.
     *
     * @return  the stored value of {@link #maxLength}
     */
    @Override
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Returns the stored value of {@link #randLength}.
     *
     * @return  the stored value of {@link #randLength}
     */
    @Override
    public int getRandLength() {
        return randLength;
    }

    /**
     * Returns the stored value of {@link #equivQueryBound}.
     *
     * @return  the stored value of {@link #equivQueryBound}
     */
    @Override
    public int getEquivQueryBound() {
        return equivQueryBound;
    }

    /**
     * Returns the stored value of {@link #runsPerMembershipQuery}.
     *
     * @return  the stored value of {@link #runsPerMembershipQuery}
     */
    @Override
    public int getRunsPerMembershipQuery() {
        return runsPerMembershipQuery;
    }

    /**
     * Returns the stored value of {@link #membershipQueryRetries}.
     *
     * @return  the stored value of {@link #membershipQueryRetries}
     */
    @Override
    public int getMembershipQueryRetries() {
        return membershipQueryRetries;
    }

    /**
     * Returns the stored value of {@link #logQueries}.
     *
     * @return  the stored value of {@link #logQueries}
     */
    @Override
    public boolean isLogQueries() {
        return logQueries;
    }

    /**
     * Returns the stored value of {@link #probReset}.
     *
     * @return  the stored value of {@link #probReset}
     */
    @Override
    public double getProbReset() {
        return probReset;
    }

    /**
     * Returns the stored value of {@link #testFile}.
     *
     * @return  the stored value of {@link #testFile}
     */
    @Override
    public String getTestFile() {
        return testFile;
    }

    /**
     * Returns the stored value of {@link #seed}.
     *
     * @return  the stored value of {@link #seed}
     */
    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the stored value of {@link #cacheTests}.
     *
     * @return  the stored value of {@link #cacheTests}
     */
    @Override
    public boolean isCacheTests() {
        return cacheTests;
    }

    /**
     * Returns the stored value of {@link #ceSanitizationDisable}.
     *
     * @return  the stored value of {@link #ceSanitizationDisable}
     */
    @Override
    public boolean isCeSanitization() {
        return !ceSanitizationDisable;
    }

    /**
     * Returns the stored value of {@link #skipNonDetTests}.
     *
     * @return  the stored value of {@link #skipNonDetTests}
     */
    @Override
    public boolean isSkipNonDetTests() {
        return skipNonDetTests;
    }

    /**
     * Returns the stored value of {@link #ceReruns}.
     *
     * @return  the stored value of {@link #ceReruns}
     */
    @Override
    public int getCeReruns() {
        return ceReruns;
    }

    /**
     * Returns the stored value of {@link #probabilisticSanitizationDisable}.
     *
     * @return  the stored value of {@link #probabilisticSanitizationDisable}
     */
    @Override
    public boolean isProbabilisticSanitization() {
        return !probabilisticSanitizationDisable;
    }

    /**
     * Returns the stored value of {@link #timeLimit}.
     *
     * @return  the stored value of {@link #timeLimit}
     */
    @Override
    public Duration getTimeLimit() {
        return timeLimit;
    }

    /**
     * Returns the stored value of {@link #testLimit}.
     *
     * @return  the stored value of {@link #testLimit}
     */
    @Override
    public Long getTestLimit() {
        return testLimit;
    }

    /**
     * Returns the stored value of {@link #roundLimit}.
     *
     * @return  the stored value of {@link #roundLimit}
     */
    @Override
    public Integer getRoundLimit() {
        return roundLimit;
    }

    /**
     * Returns the stored value of {@link #equivalenceThreadCount}.
     *
     * @return  the stored value of {@link #equivalenceThreadCount}
     */
    @Override
    public int getEquivalenceThreadCount() {
        return equivalenceThreadCount;
    }

}
