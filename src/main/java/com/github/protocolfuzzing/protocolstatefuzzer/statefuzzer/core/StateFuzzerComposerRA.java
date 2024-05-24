package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTrackerRA;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The register automata implementation of the StateFuzzerComposer interface.
 *
 * @param <B> the type of base symbols
 * @param <E> the execution context
 */
public class StateFuzzerComposerRA<B extends ParameterizedSymbol, E> implements
        StateFuzzerComposer<B, StatisticsTracker<B, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>>, RaLearningAlgorithm, IOEquivalenceOracle> {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfig learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder<B> alphabetBuilder;

    /**
     * The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}.
     */
    protected Alphabet<B> alphabet;

    /**
     * The sulOracle that is built using the SulBuilder constructor parameter,
     * wrapped using the SulWrapper constructor parameter and then wrapped
     * using DataWordSULWrapper.
     */
    protected MultiWordSULOracle sulOracle;

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
    protected StatisticsTracker<B, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>> statisticsTracker;

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
     */
    public StateFuzzerComposerRA(
            StateFuzzerEnabler stateFuzzerEnabler,
            AlphabetBuilder<B> alphabetBuilder,
            SulBuilder<PSymbolInstance, PSymbolInstance, E> sulBuilder,
            SulWrapper<PSymbolInstance, PSymbolInstance, E> sulWrapper,
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
        AbstractSul<PSymbolInstance, PSymbolInstance, E> abstractSul = sulBuilder
                .build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);

        SUL<PSymbolInstance, PSymbolInstance> sul = sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        this.sulOracle = new MultiWordSULOracle(
                new DataWordSULWrapper(sul),
                new OutputSymbol("_io_err"),
                learnerConfig.getCeReruns());

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTrackerRA<B, PSymbolInstance, Boolean>(
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
    public StateFuzzerComposerRA<B, E> initialize() {
        this.outputDir = new File(stateFuzzerEnabler.getOutputDir());
        if (!this.outputDir.exists()) {
            boolean ok = this.outputDir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outputDir);
            }
        }

        composeLearner();
        composeEquivalenceOracle();

        return this;
    }

    @Override
    public StatisticsTracker<B, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>> getStatisticsTracker() {
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
    public Alphabet<B> getAlphabet() {
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
     */
    protected void composeLearner() {
        ConstraintSolver solver = new SimpleConstraintSolver();

        this.learner = LearningSetupFactory.createRALearner(this.learnerConfig, this.sulOracle,
                this.alphabet, this.teachers, solver, this.consts);
    }

    /**
     * Composes the Equivalence Oracle and stores it in the
     * {@link #equivalenceOracle}.
     */
    protected void composeEquivalenceOracle() {
        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(this.learnerConfig,
                this.sulOracle.getDataWordSUL(), this.alphabet, this.teachers, this.consts);
    }

    /**
     * Extension of SULOracle able to return a reference to the underlying
     * DataWordSUL
     */
    protected static class MultiWordSULOracle extends SULOracle {
        /** Stores the underlying DataWordSUL */
        protected DataWordSUL sul;
        private final Integer reruns;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param sul   the underlying DataWordSUL
         * @param error the error symbol to be used
         */
        public MultiWordSULOracle(DataWordSUL sul, ParameterizedSymbol error, Integer reruns) {
            super(sul, error);
            this.sul = sul;
            this.reruns = reruns;
        }

        /**
         * Returns the underlying DataWordSUL
         *
         * @return the underlying DataWordSUL
         */
        public DataWordSUL getDataWordSUL() {
            return sul;
        }

        @Override
        public Word<PSymbolInstance> trace(Word<PSymbolInstance> input) {
            Map<Word<PSymbolInstance>, Integer> wordOccurrenceMap = new HashMap<Word<PSymbolInstance>, Integer>();
            for (int tries = 0; tries < reruns; tries++) {
                Word<PSymbolInstance> output = super.trace(input);
                Integer occurrence = wordOccurrenceMap.getOrDefault(output, 0);
                wordOccurrenceMap.put(output, occurrence + 1);
            }

            return wordOccurrenceMap
                    .entrySet()
                    .stream()
                    .max(Comparator.comparing(Entry::getValue))
                    .get().getKey();
        }
    }

    /**
     * A wrapper that can be used as an {@code SUL<PSymbolInstance,PSymbolInstance>}
     * to DataWordSUL converter.
     */
    protected static class DataWordSULWrapper extends DataWordSUL {

        /** Stores the wrapped sul */
        protected SUL<PSymbolInstance, PSymbolInstance> sul;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param sul the wrapped sul
         */
        public DataWordSULWrapper(SUL<PSymbolInstance, PSymbolInstance> sul) {
            this.sul = sul;
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
            return sul.step(in);
        }
    }
}
