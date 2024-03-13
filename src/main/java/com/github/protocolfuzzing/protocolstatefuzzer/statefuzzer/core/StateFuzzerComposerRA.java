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
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.learning.RaLearningAlgorithm;
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
import net.automatalib.word.Word;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
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

    /** The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}. */
    protected Alphabet<I> alphabet;

    /** The output for socket closed. */
    protected O socketClosedOutput;

    /**
     * The sulOracle that is built using the SulBuilder constructor parameter,
     * wrapped using the SulWrapper constructor parameter and then wrapped
     * using DataWordSULWrapper.
     */
    protected SULOracleExt sulOracle;

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
     * @param iClass             the class of the inputs
     */
    public StateFuzzerComposerRA(
        StateFuzzerEnabler stateFuzzerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SulBuilder<I, O, E> sulBuilder,
        SulWrapper<I, O, E> sulWrapper,
        @SuppressWarnings("rawtypes") Map<DataType, Theory> teachers,
        Class<I> iClass) {

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
        AbstractSul<I, O, E> abstractSul = sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);

        // initialize the output for the socket closed
        this.socketClosedOutput = abstractSul.getMapper().getOutputBuilder().buildSocketClosed();

        SUL<I, O> sul = sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        this.sulOracle = new SULOracleExt(
                new DataWordSULWrapper<I, O>(sul, iClass),
                new OutputSymbol("_io_err", new DataType[] {})
        );

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTrackerStandard<I, Boolean>(
                sulWrapper.getInputCounter(), sulWrapper.getTestCounter());
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
     * Get the SUL Oracle stored in {@link #sulOracle}
     *
     * @return a SUL Oracle (also called IO Oracle)
     */
    public SULOracle getSULOracle() {
        return sulOracle;
    }

    /**
     * Composes the Learner and stores it in the {@link #learner}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeLearner(List<O> terminatingOutputs) {
        ConstraintSolver solver = new SimpleConstraintSolver();

        this.learner = LearningSetupFactory.createRALearner(this.learnerConfig, this.sulOracle,
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
        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(this.learnerConfig,
                this.sulOracle.getDataWordSUL(), this.alphabet, this.teachers, this.consts);
    }

    /**
     * Extension of SULOracle able to return a reference to the underlying DataWordSUL
     */
    protected static class SULOracleExt extends SULOracle {
        /** Stores the underlying DataWordSUL */
        protected DataWordSUL sul;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param sul    the underlying DataWordSUL
         * @param error  the error symbol to be used
         */
        public SULOracleExt(DataWordSUL sul, ParameterizedSymbol error) {
            super(sul, error);
            this.sul = sul;
        }

        /**
         * Returns the underlying DataWordSUL
         *
         * @return  the underlying DataWordSUL
         */
        public DataWordSUL getDataWordSUL() {
            return sul;
        }
    }

    /**
    * A wrapper that can be used as an {@code SUL<I,O>} to DataWordSUL converter.
    */
    protected static class DataWordSULWrapper<I extends PSymbolInstance, O extends PSymbolInstance> extends DataWordSUL {

        /** Stores the wrapped sul */
        protected SUL<I, O> sul;

        /** Stores the class of the input symbols */
        protected Class<I> iClass;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param sul     the wrapped sul
         * @param iClass  the class of the input symbols
         */
        public DataWordSULWrapper(SUL<I, O> sul, Class<I> iClass) {
            this.sul = sul;
            this.iClass = iClass;
        }

        @Override
        public void pre() {
            sul.pre();
        }

        @Override
        public void post() {
            sul.post();
        }

        @Override
        public PSymbolInstance step(PSymbolInstance in) {
            if (!iClass.isInstance(in)) {
                throw new RuntimeException("Provided PSymbolInstance input but not of type: " + iClass.getName() +  ", got: " + in.getClass());
            }

            return (PSymbolInstance) sul.step(iClass.cast(in));
        }
    }
}
