package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTrackerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.learning.RaLearningAlgorithm;
import de.learnlib.ralib.oracles.DataWordOracle;
import de.learnlib.ralib.oracles.io.IOOracle;
import de.learnlib.ralib.solver.ConstraintSolver;
import de.learnlib.ralib.solver.simple.SimpleConstraintSolver;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The register automata implementation of the StateFuzzerComposer interface.
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 * @param <E> the execution context
 */
public class StateFuzzerComposerRA<I extends PSymbolInstance, O extends PSymbolInstance, E> implements
        StateFuzzerComposer<I, StatisticsTracker<I, Word<I>, Boolean, DefaultQuery<I, Boolean>>, RaLearningAlgorithm, IOEquivalenceOracle> {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfig learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder<I> alphabetBuilder;

    /**
     * The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}.
     */
    protected Alphabet<I> alphabet;

    /** The output for socket closed. */
    protected O socketClosedOutput;

    /**
     * The sul that is built using the SulBuilder constructor parameter and
     * wrapped using the SulWrapper constructor parameter.
     */
    protected SUL<I, O> sul;

    /**
     * The teachers for the RALib learning algorithm.
     * Note: Theory is used as a rawtype like this in RALib as theories of different
     * types can be used for the same learner so we don't know how to solve this
     * warning
     */
    @SuppressWarnings("rawtypes")
    protected Map<DataType, Theory> teachers;

    /** Constants used by the RALib learning algorithm. */
    protected Constants consts;

    /** The output directory from the {@link #stateFuzzerEnabler}. */
    protected File outputDir;

    /** The cleanup tasks of the composer. */
    protected CleanupTasks cleanupTasks;

    /** The statistics tracker that is composed. */
    protected StatisticsTracker<I, Word<I>, Boolean, DefaultQuery<I, Boolean>> statisticsTracker;

    /** The learner that is composed. */
    protected RaLearningAlgorithm learner;

    /** The equivalence oracle that is composed. */
    protected IOEquivalenceOracle equivalenceOracle;

    /** The IO oracle that is composed. */
    protected IOOracle ioOracle;

    /** The DataWord oracle. */
    protected DataWordOracle dWordOracle;

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
     * @param teachers           the teachers to be used
     */
    public StateFuzzerComposerRA(StateFuzzerEnabler stateFuzzerEnabler,
            AlphabetBuilder<I> alphabetBuilder,
            SulBuilder<I, O, E> sulBuilder,
            SulWrapper<I, O, E> sulWrapper,
            // Theory is used as a rawtype like this in RALib as theories of different types
            // can be used for the same learner so we don't know how to solve this warning
            @SuppressWarnings("rawtypes") Map<DataType, Theory> teachers) {
        this.stateFuzzerEnabler = stateFuzzerEnabler;
        this.learnerConfig = stateFuzzerEnabler.getLearnerConfig();

        // de-serialize and build alphabet
        this.alphabetBuilder = alphabetBuilder;
        this.alphabet = alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());

        // initialize cleanup tasks
        this.cleanupTasks = new CleanupTasks();

        this.consts = new Constants();

        this.teachers = teachers;

        // set up wrapped SUL (System Under Learning)
        // FIXME: Dangerous cast
        AbstractSul<I, O, E> abstractSul = sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);

        // initialize the output for the socket closed
        this.socketClosedOutput = abstractSul.getMapper().getOutputBuilder().buildSocketClosed();

        this.sul = sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTrackerStandard<I, Boolean>(
                sulWrapper.getInputCounter(), sulWrapper.getTestCounter());

        // TODO: Figure out how to create dwOracle
        this.dWordOracle = new DataWordOracle() {

            @Override
            public void processQueries(Collection<? extends Query<PSymbolInstance, Boolean>> arg0) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'processQueries'");
            }
        };
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
    public StateFuzzerComposerRA<I, O, E> initialize() {
        this.outputDir = new File(stateFuzzerEnabler.getOutputDir());
        if (!this.outputDir.exists()) {
            boolean ok = this.outputDir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outputDir);
            }
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
    public StatisticsTracker<I, Word<I>, Boolean, DefaultQuery<I, Boolean>> getStatisticsTracker() {
        return statisticsTracker;
    }

    @Override
    public RaLearningAlgorithm getLearner() {
        return learner;
    }

    @Override
    public IOEquivalenceOracle getEquivalenceOracle() {
        return equivalenceOracle;
    }

    @Override
    public Alphabet<I> getAlphabet() {
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
     * Get the SUL Oracle stored in {@link #ioOracle}
     *
     * @return a SUL Oracle (also called IO Oracle)
     */
    public IOOracle getSULOracle() {
        return ioOracle;
    }

    /**
     * Composes the Learner and stores it in the {@link #learner}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeLearner(List<O> terminatingOutputs) {
        // TODO: Compose caching/logging oracles


        ConstraintSolver solver = new SimpleConstraintSolver();

        this.learner = LearningSetupFactory.createRALearner(this.learnerConfig, this.dWordOracle,
                this.alphabet, this.teachers, solver, this.consts);
    }

    /**
     * Composes the Equivalence Oracle and stores it in the
     * {@link #equivalenceOracle}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeEquivalenceOracle(List<O> terminatingOutputs) {

        // TODO: Consider adding logging/caching oracles
        // NOTE: If something explodes look at this cast, it is unreasonable for the
        // compiler to believe that this is safe.
        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(this.learnerConfig,
                (DataWordSUL) this.sul, this.dWordOracle,
                this.alphabet, this.teachers, this.consts);
    }

    /**
     * Composes the SUL Oracle and stores it in the {@link #ioOracle}
     */
    protected void composeSULOracle() {
        this.ioOracle = new SULOracle((DataWordSUL) this.sul, new OutputSymbol("_io_err", new DataType[] {}));
    }
}
