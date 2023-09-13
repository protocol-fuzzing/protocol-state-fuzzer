package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The standard implementation of the StateFuzzerComposer Interface.
 */
public class StateFuzzerComposerStandard implements StateFuzzerComposer {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfig learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder alphabetBuilder;

    /** The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}. */
    protected Alphabet<AbstractInput> alphabet;

    /**
     * The sul that is built using the SulBuilder constructor parameter and
     * wrapped using the SulWrapper constructor parameter.
     */
    protected SUL<AbstractInput, AbstractOutput> sul;

    /** The cache used by the learning oracles. */
    protected ObservationTree<AbstractInput, AbstractOutput> cache;

    /** The output directory from the {@link #stateFuzzerEnabler}. */
    protected File outputDir;

    /** The file writer of the non determinism case. */
    protected FileWriter nonDetWriter;

    /** The cleanup tasks of the composer. */
    protected CleanupTasks cleanupTasks;

    /** The statistics tracker that is composed. */
    protected StatisticsTracker statisticsTracker;

    /** The learner that is composed. */
    protected LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> learner;

    /** The equivalence oracle that is composed. */
    protected EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>>
        equivalenceOracle;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Specifically the learning components are set up:
     * <ul>
     * <li> the alphabet is built using the AlphabetBuilder parameter
     * <li> the sul is built using the SulBuilder parameter and the SulWrapper parameter
     * <li> the StatisticsTracker is composed
     * <li> the Learner is composed
     * <li> the Equivalence Oracle is composed
     * </ul>
     *
     * @param stateFuzzerEnabler  the configuration that enables the state fuzzing
     * @param alphabetBuilder     the builder of the alphabet
     * @param sulBuilder          the builder of the sul
     * @param sulWrapper          the wrapper of the sul
     */
    public StateFuzzerComposerStandard(StateFuzzerEnabler stateFuzzerEnabler, AlphabetBuilder alphabetBuilder,
                                       SulBuilder sulBuilder, SulWrapper sulWrapper){
        this.stateFuzzerEnabler = stateFuzzerEnabler;
        this.learnerConfig = stateFuzzerEnabler.getLearnerConfig();

        // de-serialize and build alphabet
        this.alphabetBuilder = alphabetBuilder;
        this.alphabet = alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());

        // set up output directory
        this.outputDir = new File(stateFuzzerEnabler.getOutputDir());
        if (!this.outputDir.exists()) {
            boolean ok = this.outputDir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outputDir);
            }
        }

        // initialize cleanup tasks
        this.cleanupTasks = new CleanupTasks();

        // set up wrapped SUL (System Under Learning)
        AbstractSul abstractSul = sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);
        this.sul = sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        // TODO the LOGGER instances should handle this, instead of passing non det writers as arguments.
        try {
            this.nonDetWriter = new FileWriter(new File(outputDir, NON_DET_FILENAME), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not create non-determinism file writer");
        }

        List<AbstractOutput> cacheTerminatingOutputs = new ArrayList<>();
        if (stateFuzzerEnabler.getSulConfig().getMapperConfig().isSocketClosedAsTimeout()) {
            cacheTerminatingOutputs.add(AbstractOutput.socketClosed());
        }

        // initialize cache as observation tree
        this.cache = new ObservationTree<>();

        // compose statistics tracker, learner and equivalence oracle in this specific order
        composeStatisticsTracker(sulWrapper.getInputCounter(), sulWrapper.getTestCounter());
        composeLearner(cacheTerminatingOutputs);
        composeEquivalenceOracle(cacheTerminatingOutputs);
    }

    @Override
    public StatisticsTracker getStatisticsTracker() {
        return statisticsTracker;
    }

    @Override
    public LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> getLearner() {
        return learner;
    }

    @Override
    public EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>>
    getEquivalenceOracle() {
        return equivalenceOracle;
    }

    @Override
    public Alphabet<AbstractInput> getAlphabet(){
        return alphabet;
    }

    @Override
    public InputStream getAlphabetFileInputStream() {
        return alphabetBuilder.getAlphabetFileInputStream(learnerConfig);
    }

    @Override
    public String getAlphabetFileExtension() {
        return alphabetBuilder.getAlphabetFileExtension();
    }

    @Override
    public StateFuzzerEnabler getStateFuzzerEnabler() {
        return stateFuzzerEnabler;
    }

    @Override
    public File getOutputDir() {
        return outputDir;
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    /**
     * Composes the statistics tracker and stores it in the {@link #statisticsTracker}.
     *
     * @param inputCounter  the counter for the membership queries
     * @param testCounter   the counter for the equivalence queries
     */
    protected void composeStatisticsTracker(Counter inputCounter, Counter testCounter) {
        this.statisticsTracker = new StatisticsTracker(inputCounter, testCounter);
    }

    /**
     * Composes the Learner and stores it in the {@link #learner}.
     *
     * @param terminatingOutputs  the terminating outputs used by the {@link CachingSULOracle}
     */
    protected void composeLearner(List<AbstractOutput> terminatingOutputs) {

        MembershipOracle.MealyMembershipOracle<AbstractInput, AbstractOutput> learningSulOracle = new SULOracle<>(sul);

        if (learnerConfig.getRunsPerMembershipQuery() > 1) {
            learningSulOracle = new MultipleRunsSULOracle<>(learnerConfig.getRunsPerMembershipQuery(),
                    learningSulOracle,true, nonDetWriter);
        }

        // an oracle which uses the cache to check for non-determinism
        // and re-runs queries if non-determinism is detected
        learningSulOracle = new NonDeterminismRetryingSULOracle<>(
            learnerConfig.getMembershipQueryRetries(), learningSulOracle, true, nonDetWriter, cache);

        // we are adding a cache so that executions of same inputs aren't repeated
        learningSulOracle = new CachingSULOracle<>(learningSulOracle, cache, false, terminatingOutputs);

        FileWriter queryWriter = null;
        if (learnerConfig.isLogQueries()) {
            try {
                queryWriter = new FileWriter(new File(outputDir, QUERY_FILENAME), StandardCharsets.UTF_8);
            } catch (IOException e1) {
                throw new RuntimeException("Could not create queryfile writer");
            }
        }
        learningSulOracle = new LoggingSULOracle<>(learningSulOracle, queryWriter);

        this.learner = LearningSetupFactory.createMealyLearner(learnerConfig, learningSulOracle, alphabet);
    }

    /**
     * Composes the Equivalence Oracle and stores it in the {@link #equivalenceOracle}.
     *
     * @param terminatingOutputs  the terminating outputs used by the {@link CachingSULOracle}
     */
    protected void composeEquivalenceOracle(List<AbstractOutput> terminatingOutputs) {

        MembershipOracle.MealyMembershipOracle<AbstractInput, AbstractOutput> equivalenceSulOracle = new SULOracle<>(sul);

        // in case sanitization is enabled, we apply a CE verification wrapper
        // to check counterexamples before they are returned to the EQ oracle
        if (learnerConfig.isCeSanitization()) {
            equivalenceSulOracle = new CESanitizingSULOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, AbstractOutput>(
                learnerConfig.getCeReruns(), equivalenceSulOracle, learnerConfig.isProbabilisticSanitization(),
                nonDetWriter, learner::getHypothesisModel, cache, learnerConfig.isSkipNonDetTests());
        }

        // we are adding a cache and a logging oracle
        equivalenceSulOracle = new CachingSULOracle<>(equivalenceSulOracle, cache, !learnerConfig.isCacheTests(), terminatingOutputs);
        equivalenceSulOracle = new LoggingSULOracle<>(equivalenceSulOracle);

        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(learnerConfig, sul, equivalenceSulOracle, alphabet);
    }
}
