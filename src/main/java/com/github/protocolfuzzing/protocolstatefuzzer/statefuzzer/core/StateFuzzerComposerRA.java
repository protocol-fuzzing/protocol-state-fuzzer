package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.RALearner;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.oracles.DataWordOracle;
import de.learnlib.ralib.oracles.io.IOOracle;
import de.learnlib.ralib.solver.ConstraintSolver;
import de.learnlib.ralib.solver.simple.SimpleConstraintSolver;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The standard implementation of the StateFuzzerComposer Interface.
 */
public class StateFuzzerComposerRA implements StateFuzzerComposer<IOEquivalenceOracle, ParameterizedSymbol, RALearner> {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfigRA learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder alphabetBuilder;

    /**
     * The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}.
     */
    protected Alphabet<ParameterizedSymbol> alphabet;

    /**
     * The sul that is built using the SulBuilder constructor parameter and
     * wrapped using the SulWrapper constructor parameter.
     */
    protected SUL<PSymbolInstance, PSymbolInstance> sul;

    /** The cache used by the learning oracles. */
    protected ObservationTree<PSymbolInstance, PSymbolInstance> cache;

    /** The output directory from the {@link #stateFuzzerEnabler}. */
    protected File outputDir;

    /** The file writer of the non determinism case. */
    protected FileWriter nonDetWriter;

    /** The cleanup tasks of the composer. */
    protected CleanupTasks cleanupTasks;

    /** The statistics tracker that is composed. */
    protected StatisticsTracker statisticsTracker;

    /** The learner that is composed. */
    protected RALearner learner;

    /** The equivalence oracle that is composed. */
    protected IOEquivalenceOracle equivalenceOracle;

    protected IOOracle ioOracle;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Specifically:
     * <ul>
     * <li>the alphabet is built using the AlphabetBuilder parameter
     * <li>the sul is built using the SulBuilder parameter and the SulWrapper
     * parameter
     * <li>the StatisticsTracker is created
     * </ul>
     * <p>
     * Invoke {@link #initialize()} afterwards.
     *
     * @param stateFuzzerEnabler the configuration that enables the state fuzzing
     * @param alphabetBuilder    the builder of the alphabet
     * @param sulBuilder         the builder of the sul
     * @param sulWrapper         the wrapper of the sul
     */
    public StateFuzzerComposerRA(StateFuzzerEnabler stateFuzzerEnabler, AlphabetBuilder alphabetBuilder,
            SulBuilder sulBuilder, SulWrapper sulWrapper) {
        this.stateFuzzerEnabler = stateFuzzerEnabler;
        this.learnerConfig = stateFuzzerEnabler.getLearnerConfig();

        // de-serialize and build alphabet
        this.alphabetBuilder = alphabetBuilder;
        // TODO: Make compatible with RA
        this.alphabet = null; // alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());

        // initialize cleanup tasks
        this.cleanupTasks = new CleanupTasks();

        // set up wrapped SUL (System Under Learning)
        AbstractSul abstractSul = sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);
        // TODO: Make compatible with RA
        // this.sul = sulWrapper
        // .wrap(abstractSul)
        // .setTimeLimit(learnerConfig.getTimeLimit())
        // .setTestLimit(learnerConfig.getTestLimit())
        // .setLoggingWrapper("")
        // .getWrappedSul();

        // initialize cache as observation tree
        this.cache = new ObservationTree<>();

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTracker(sulWrapper.getInputCounter(), sulWrapper.getTestCounter());
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * <p>
     * Specifically:
     * <ul>
     * <li>the output directory is created if needed
     * <li>the Learner is composed
     * <li>the Equivalence Oracle is composed
     * </ul>
     *
     * @return the same instance
     */
    public StateFuzzerComposerRA initialize() {
        this.outputDir = new File(stateFuzzerEnabler.getOutputDir());
        if (!this.outputDir.exists()) {
            boolean ok = this.outputDir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outputDir);
            }
        }

        // TODO the LOGGER instances should handle this, instead of passing non det
        // writers as arguments.
        try {
            this.nonDetWriter = new FileWriter(new File(outputDir, NON_DET_FILENAME), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not create non-determinism file writer");
        }

        List<AbstractOutput> cacheTerminatingOutputs = new ArrayList<>();
        if (stateFuzzerEnabler.getSulConfig().getMapperConfig().isSocketClosedAsTimeout()) {
            cacheTerminatingOutputs.add(AbstractOutput.socketClosed());
        }

        composeLearner(cacheTerminatingOutputs);
        composeEquivalenceOracle(cacheTerminatingOutputs);

        return this;
    }

    @Override
    public StatisticsTracker getStatisticsTracker() {
        return statisticsTracker;
    }

    @Override
    public RALearner getLearner() {
        return learner;
    }

    @Override
    public IOEquivalenceOracle getEquivalenceOracle() {
        return equivalenceOracle;
    }

    @Override
    public Alphabet<ParameterizedSymbol> getAlphabet() {
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

    public IOOracle getSULOracle() {
        return ioOracle;
    }

    /**
     * Composes the Learner and stores it in the {@link #learner}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeLearner(List<AbstractOutput> terminatingOutputs) {
        // TODO: Compose caching/logging oracles
        // MembershipOracle.MealyMembershipOracle<AbstractInput, AbstractOutput>
        // learningSulOracle = new SULOracle<>(sul);

        // if (learnerConfig.getRunsPerMembershipQuery() > 1) {
        // learningSulOracle = new
        // MultipleRunsSULOracle<>(learnerConfig.getRunsPerMembershipQuery(),
        // learningSulOracle,true, nonDetWriter);
        // }

        // // an oracle which uses the cache to check for non-determinism
        // // and re-runs queries if non-determinism is detected
        // learningSulOracle = new NonDeterminismRetryingSULOracle<>(
        // learnerConfig.getMembershipQueryRetries(), learningSulOracle, true,
        // nonDetWriter, cache);

        // // we are adding a cache so that executions of same inputs aren't repeated
        // learningSulOracle = new CachingSULOracle<>(learningSulOracle, cache, false,
        // terminatingOutputs);

        // FileWriter queryWriter = null;
        // if (learnerConfig.isLogQueries()) {
        // try {
        // queryWriter = new FileWriter(new File(outputDir, QUERY_FILENAME),
        // StandardCharsets.UTF_8);
        // } catch (IOException e1) {
        // throw new RuntimeException("Could not create queryfile writer");
        // }
        // }
        // learningSulOracle = new LoggingSULOracle<>(learningSulOracle, queryWriter);

        final DataWordOracle dwOracle = new DataWordOracle() {};
        Map<DataType, Theory> teachers;
        Constants consts = new Constants();
        ConstraintSolver solver = new SimpleConstraintSolver();

        this.learner = new RALearner(LearningSetupFactory.createRALearner(this.learnerConfig, dwOracle, this.alphabet, teachers, solver, consts), alphabet);
    }

    /**
     * Composes the Equivalence Oracle and stores it in the
     * {@link #equivalenceOracle}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeEquivalenceOracle(List<AbstractOutput> terminatingOutputs) {

        // TODO: Consider adding logging/caching oracles
        // MembershipOracle.MealyMembershipOracle<AbstractInput, AbstractOutput>
        // equivalenceSulOracle = new SULOracle<>(sul);

        // in case sanitization is enabled, we apply a CE verification wrapper
        // to check counterexamples before they are returned to the EQ oracle
        // if (learnerConfig.isCeSanitization()) {
        // equivalenceSulOracle = new CESanitizingSULOracle<MealyMachine<?,
        // AbstractInput, ?, AbstractOutput>, AbstractInput, AbstractOutput>(
        // learnerConfig.getCeReruns(), equivalenceSulOracle,
        // learnerConfig.isProbabilisticSanitization(),
        // nonDetWriter, learner::getHypothesisModel, cache,
        // learnerConfig.isSkipNonDetTests());
        // }

        // // we are adding a cache and a logging oracle
        // equivalenceSulOracle = new CachingSULOracle<>(equivalenceSulOracle, cache,
        // !learnerConfig.isCacheTests(), terminatingOutputs);
        // equivalenceSulOracle = new LoggingSULOracle<>(equivalenceSulOracle);

        // TODO
        final DataWordOracle dwOracle = new DataWordOracle() {};
        Map<DataType, Theory> teachers;
        
        Constants consts = new Constants();

        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(this.learnerConfig, (DataWordSUL) this.sul, dwOracle, alphabet, teachers, consts);
    }

    protected void composeSULOracle() {
        IOOracle ioOracle = new SULOracle((DataWordSUL) this.sul, new OutputSymbol("_io_err", new DataType[] {}));

    }
}
