package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

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
public class StateFuzzerComposerStandard<S, I, O extends MapperOutput<O, P>, P> implements StateFuzzerComposer<I, O> {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfig learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder<I> alphabetBuilder;

    /** The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}. */
    protected Alphabet<I> alphabet;

    /**
     * The sul that is built using the SulBuilder constructor parameter and
     * wrapped using the SulWrapper constructor parameter.
     */
    protected SUL<I, O> sul;

    /** The cache used by the learning oracles. */
    protected ObservationTree<I, O> cache;

    /** The output for socket closed. */
    protected O socketClosedOutput;

    /** The output directory from the {@link #stateFuzzerEnabler}. */
    protected File outputDir;

    /** The file writer of the non determinism case. */
    protected FileWriter nonDetWriter;

    /** The cleanup tasks of the composer. */
    protected CleanupTasks cleanupTasks;

    /** The statistics tracker that is composed. */
    protected StatisticsTracker<I, O> statisticsTracker;

    /** The learner that is composed. */
    protected LearningAlgorithm.MealyLearner<I, O> learner;

    /** The equivalence oracle that is composed. */
    protected EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
        equivalenceOracle;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Specifically:
     * <ul>
     * <li> the alphabet is built using the AlphabetBuilder parameter
     * <li> the sul is built using the SulBuilder parameter and the SulWrapper parameter
     * <li> the StatisticsTracker is created
     * </ul>
     * <p>
     * Invoke {@link #initialize()} afterwards.
     *
     * @param stateFuzzerEnabler  the configuration that enables the state fuzzing
     * @param alphabetBuilder     the builder of the alphabet
     * @param sulBuilder          the builder of the sul
     * @param sulWrapper          the wrapper of the sul
     */
    public StateFuzzerComposerStandard(
        StateFuzzerEnabler stateFuzzerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SulBuilder<S, I, O> sulBuilder,
        SulWrapper<S, I, O> sulWrapper
    ){
        this.stateFuzzerEnabler = stateFuzzerEnabler;
        this.learnerConfig = stateFuzzerEnabler.getLearnerConfig();

        // de-serialize and build alphabet
        this.alphabetBuilder = alphabetBuilder;
        this.alphabet = alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());

        // initialize cleanup tasks
        this.cleanupTasks = new CleanupTasks();

        // set up wrapped SUL (System Under Learning)
        AbstractSul<S, I, O> abstractSul = sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);
        this.sul = sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        // initialize the output for the socket closed
        this.socketClosedOutput = abstractSul.getMapper().getOutputBuilder().buildSocketClosed();

        // initialize cache as observation tree
        this.cache = new ObservationTree<>();

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTracker<>(sulWrapper.getInputCounter(), sulWrapper.getTestCounter());
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * <p>
     * Specifically:
     * <ul>
     * <li> the output directory is created if needed
     * <li> the Learner is composed
     * <li> the Equivalence Oracle is composed
     * </ul>
     *
     * @return  the same instance
     */
    public StateFuzzerComposerStandard<S, I, O, P> initialize() {
        this.outputDir = new File(stateFuzzerEnabler.getOutputDir());
        if (!this.outputDir.exists()) {
            boolean ok = this.outputDir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outputDir);
            }
        }

        // TODO the LOGGER instances should handle this, instead of passing non det writers as arguments.
        try {
            this.nonDetWriter = new FileWriter(new File(outputDir, NON_DET_FILENAME), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not create non-determinism file writer");
        }

        List<O> cacheTerminatingOutputs = new ArrayList<>();
        if (stateFuzzerEnabler.getSulConfig().getMapperConfig().isSocketClosedAsTimeout()) {
            cacheTerminatingOutputs.add(socketClosedOutput);
        }

        composeLearner(cacheTerminatingOutputs);
        composeEquivalenceOracle(cacheTerminatingOutputs);

        return this;
    }

    @Override
    public StatisticsTracker<I, O> getStatisticsTracker() {
        return statisticsTracker;
    }

    @Override
    public LearningAlgorithm.MealyLearner<I, O> getLearner() {
        return learner;
    }

    @Override
    public EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
    getEquivalenceOracle() {
        return equivalenceOracle;
    }

    @Override
    public Alphabet<I> getAlphabet(){
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
     * Composes the Learner and stores it in the {@link #learner}.
     *
     * @param terminatingOutputs  the terminating outputs used by the {@link CachingSULOracle}
     */
    protected void composeLearner(List<O> terminatingOutputs) {

        MembershipOracle.MealyMembershipOracle<I, O> learningSulOracle = new SULOracle<>(sul);

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
    protected void composeEquivalenceOracle(List<O> terminatingOutputs) {

        MembershipOracle.MealyMembershipOracle<I, O> equivalenceSulOracle = new SULOracle<>(sul);

        // in case sanitization is enabled, we apply a CE verification wrapper
        // to check counterexamples before they are returned to the EQ oracle
        if (learnerConfig.isCeSanitization()) {
            equivalenceSulOracle = new CESanitizingSULOracle<MealyMachine<?, I, ?, O>, I, O>(
                learnerConfig.getCeReruns(), equivalenceSulOracle, learnerConfig.isProbabilisticSanitization(),
                nonDetWriter, learner::getHypothesisModel, cache, learnerConfig.isSkipNonDetTests());
        }

        // we are adding a cache and a logging oracle
        equivalenceSulOracle = new CachingSULOracle<>(equivalenceSulOracle, cache, !learnerConfig.isCacheTests(), terminatingOutputs);
        equivalenceSulOracle = new LoggingSULOracle<>(equivalenceSulOracle);

        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(learnerConfig, sul, equivalenceSulOracle, alphabet);
    }
}
