package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.RALearner;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTrackerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSulRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.oracles.DataWordOracle;
import de.learnlib.ralib.oracles.io.IOOracle;
import de.learnlib.ralib.solver.ConstraintSolver;
import de.learnlib.ralib.solver.simple.SimpleConstraintSolver;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The standard implementation of the StateFuzzerComposer Interface.
 */
public class StateFuzzerComposerRA implements
        StateFuzzerComposer<PSymbolInstance, StatisticsTracker<PSymbolInstance, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>>, RALearner, IOEquivalenceOracle> {

    /** Stores the constructor parameter. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** The LearnerConfig from the {@link #stateFuzzerEnabler}. */
    protected LearnerConfig learnerConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder<PSymbolInstance> alphabetBuilder;

    /**
     * The built alphabet using {@link #alphabetBuilder} and {@link #learnerConfig}.
     */
    protected Alphabet<PSymbolInstance> alphabet;

    /** The output for socket closed. */
    protected PSymbolInstance socketClosedOutput;

    /**
     * The sul that is built using the SulBuilder constructor parameter and
     * wrapped using the SulWrapper constructor parameter.
     */
    protected AbstractSulRA sul;
    
    // Theory is used as a rawtype like this in RALib as theories of different types can be used for the same learner so we don't know how to solve this warning
    @SuppressWarnings("rawtypes")
    protected Map<DataType, Theory> teachers;

    protected Constants consts;

    /** The cache used by the learning oracles. */
    // TODO: Replace with RA cache instead? Or does this work for RA?
    protected ObservationTree<PSymbolInstance, PSymbolInstance> cache;

    /** The output directory from the {@link #stateFuzzerEnabler}. */
    protected File outputDir;

    /** The file writer of the non determinism case. */
    protected FileWriter nonDetWriter;

    /** The cleanup tasks of the composer. */
    protected CleanupTasks cleanupTasks;

    /** The statistics tracker that is composed. */
    protected StatisticsTracker<PSymbolInstance, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>> statisticsTracker;

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
    public StateFuzzerComposerRA(StateFuzzerEnabler stateFuzzerEnabler,
            AlphabetBuilder<PSymbolInstance> alphabetBuilder,
            SulBuilder<PSymbolInstance, PSymbolInstance, IOEquivalenceOracle> sulBuilder,
            SulWrapper<PSymbolInstance, PSymbolInstance, IOEquivalenceOracle> sulWrapper,
            // Theory is used as a rawtype like this in RALib as theories of different types can be used for the same learner so we don't know how to solve this warning
            @SuppressWarnings("rawtypes") Map<DataType, Theory> teachers) {
        this.stateFuzzerEnabler = stateFuzzerEnabler;
        this.learnerConfig = stateFuzzerEnabler.getLearnerConfig();

        // de-serialize and build alphabet
        this.alphabetBuilder = alphabetBuilder;
        // TODO: Make compatible with RA
        this.alphabet = alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());

        // initialize cleanup tasks
        this.cleanupTasks = new CleanupTasks();

        this.consts = new Constants();

        this.teachers = teachers;

        // set up wrapped SUL (System Under Learning)
        // FIXME: Dangerous cast
        AbstractSulRA abstractSul = (AbstractSulRA) sulBuilder.build(stateFuzzerEnabler.getSulConfig(), cleanupTasks);

        // TODO: Make compatible with RA
        this.sul = (AbstractSulRA) sulWrapper
                .wrap(abstractSul)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSul();

        // initialize cache as observation tree
        // TODO: Replace with RA cache instead? Or does this work for RA?
        this.cache = new ObservationTree<>();

        // initialize statistics tracker
        this.statisticsTracker = new StatisticsTrackerStandard<PSymbolInstance, Boolean>(
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

        List<PSymbolInstance> cacheTerminatingOutputs = new ArrayList<>();
        if (stateFuzzerEnabler.getSulConfig().getMapperConfig().isSocketClosedAsTimeout()) {
            cacheTerminatingOutputs.add(socketClosedOutput);
        }

        composeLearner(cacheTerminatingOutputs);
        composeEquivalenceOracle(cacheTerminatingOutputs);

        return this;
    }

    @Override
    public StatisticsTracker<PSymbolInstance, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>> getStatisticsTracker() {
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
    public Alphabet<PSymbolInstance> getAlphabet() {
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
    protected void composeLearner(List<PSymbolInstance> terminatingOutputs) {
        // TODO: Compose caching/logging oracles

        final DataWordOracle dwOracle = new DataWordOracle() {

            @Override
            public void processQueries(Collection<? extends Query<PSymbolInstance, Boolean>> arg0) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'processQueries'");
            }
        };
        ConstraintSolver solver = new SimpleConstraintSolver();

        this.learner = new RALearner(
                LearningSetupFactory.createRALearner(this.learnerConfig, dwOracle,
                        this.alphabet, this.teachers, solver, this.consts),
                this.alphabet);
    }

    /**
     * Composes the Equivalence Oracle and stores it in the
     * {@link #equivalenceOracle}.
     *
     * @param terminatingOutputs the terminating outputs used by the
     *                           {@link CachingSULOracle}
     */
    protected void composeEquivalenceOracle(List<PSymbolInstance> terminatingOutputs) {

        // TODO: Consider adding logging/caching oracles

        // TODO: Figure out how to create dwOracle
        DataWordOracle dwOracle = new DataWordOracle() {

            @Override
            public void processQueries(Collection<? extends Query<PSymbolInstance, Boolean>> arg0) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'processQueries'");
            }
        };

        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(this.learnerConfig, this.sul, dwOracle,
                this.alphabet, this.teachers, this.consts);
    }

    protected void composeSULOracle() {
        this.ioOracle = new SULOracle(this.sul, new OutputSymbol("_io_err", new DataType[] {}));
    }
}
