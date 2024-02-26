package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.RoundLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TestLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TimeLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.Statistics;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.equivalence.IOCounterExamplePrefixFinder;
import de.learnlib.ralib.equivalence.IOCounterExamplePrefixReplacer;
import de.learnlib.ralib.equivalence.IOCounterexampleLoopRemover;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.learning.Hypothesis;
import de.learnlib.ralib.learning.RaLearningAlgorithm;
import de.learnlib.ralib.oracles.io.IOOracle;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * The register automata implementation of the StateFuzzer interface.
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 * @param <E> the execution context
 */
public class StateFuzzerRA<I extends PSymbolInstance, O extends PSymbolInstance, E>
        implements StateFuzzer<RegisterAutomatonWrapper<I>> {
    /**
     * TODO: Missing docs
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The filename of the alphabet with the extension from
     * {@link #stateFuzzerComposer}.
     */
    protected final String ALPHABET_FILENAME;

    /** Stores the constructor parameter. */
    protected StateFuzzerComposerRA<I, O, E> stateFuzzerComposer;

    /** The alphabet from the {@link #stateFuzzerComposer}. */
    protected Alphabet<I> alphabet;

    /** The output directory from the {@link #stateFuzzerComposer}. */
    protected File outputDir;

    /** The cleanup tasks from the {@link #stateFuzzerComposer}. */
    protected CleanupTasks cleanupTasks;

    /** The StateFuzzerEnabler from the {@link #stateFuzzerComposer}. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /**
     * Creates a new state fuzzer with the components specified in the StateFuzzerComposer.
     * @param stateFuzzerComposer the StateFuzzerComposer to use
     */
    public StateFuzzerRA(StateFuzzerComposerRA<I, O, E> stateFuzzerComposer) {
        this.stateFuzzerComposer = stateFuzzerComposer;
        this.stateFuzzerEnabler = stateFuzzerComposer.getStateFuzzerEnabler();
        this.alphabet = stateFuzzerComposer.getAlphabet();
        this.outputDir = stateFuzzerComposer.getOutputDir();
        this.cleanupTasks = stateFuzzerComposer.getCleanupTasks();
        this.ALPHABET_FILENAME = ALPHABET_FILENAME_NO_EXTENSION + stateFuzzerComposer.getAlphabetFileExtension();
    }

    @Override
    public LearnerResult<RegisterAutomatonWrapper<I>> startFuzzing() {
        try {
            return inferRegisterAutomata();
        } catch (RuntimeException e) {
            LOGGER.error("Exception encountered during state fuzzing");
            throw e;
        } finally {
            cleanupTasks.execute();
        }
    }

    /**
     * Uses the RALib learning components for the state fuzzing.
     * <p>
     * Also it copies the necessary files, proceeds with the state fuzzing and
     * exports the final statistics.
     *
     * @return the corresponding LearnerResult, which can be empty if state
     *         fuzzing fails
     */
    @SuppressWarnings("unchecked") // TODO: remove this
    protected LearnerResult<RegisterAutomatonWrapper<I>> inferRegisterAutomata() {
        // for convenience, we copy all the input files/streams
        // to the output directory before starting the arduous learning process
        copyInputsToOutputDir(outputDir);

        // setting up statistics tracker, learner and equivalence oracle
        StatisticsTracker<I, Word<I>, Boolean, DefaultQuery<I, Boolean>> statisticsTracker = stateFuzzerComposer
                .getStatisticsTracker();

        RaLearningAlgorithm learner = stateFuzzerComposer.getLearner();
        IOEquivalenceOracle equivalenceOracle = stateFuzzerComposer.getEquivalenceOracle();
        RegisterAutomatonWrapper<I> hypothesis = null;
        LearnerResult<RegisterAutomatonWrapper<I>> learnerResult = new LearnerResult<RegisterAutomatonWrapper<I>>();

        DefaultQuery<PSymbolInstance, Boolean> counterExample = null;

        boolean finished = false;
        String notFinishedReason = null;
        int current_round = 0;
        int round_limit = roundLimitToInt(stateFuzzerEnabler.getLearnerConfig().getRoundLimit());

        try {
            statisticsTracker
                    .setRuntimeStateTracking(new FileOutputStream(new File(outputDir, LEARNING_STATE_FILENAME)));
        } catch (FileNotFoundException e1) {
            throw new RuntimeException("Could not create runtime state tracking output stream");
        }

        try {
            LOGGER.info("Input alphabet: {}", alphabet);
            LOGGER.info("Starting Learning" + System.lineSeparator());

            statisticsTracker.startLearning(stateFuzzerEnabler, alphabet);

            learner.learn();
            current_round++;

            do {
                RegisterAutomaton hyp = learner.getHypothesis();
                hypothesis = new RegisterAutomatonWrapper<I>(hyp, this.alphabet);

                learnerResult.addHypothesis(hypothesis);
                // it is useful to print intermediate hypothesis as learning is running
                String hypName = "hyp" + current_round + ".dot";
                exportHypothesis(hypothesis, new File(outputDir, hypName));
                statisticsTracker.newHypothesis(hypothesis);
                LOGGER.info("Generated new hypothesis: " + hypName);

                if (current_round == round_limit) {
                    // round_limit can be either -1 (no limit) or a positive int
                    throw new RoundLimitReachedException(round_limit);
                }

                LOGGER.info("Validating hypothesis" + System.lineSeparator());
                counterExample = equivalenceOracle.findCounterExample(hypothesis.getRegisterAutomaton(), null);

                if (counterExample != null) {
                    LOGGER.info("Counterexample: " + counterExample);
                    statisticsTracker.newCounterExample((DefaultQuery<I, Boolean>) counterExample);
                    // we create a copy, since the hypothesis reference will not be valid after
                    // refinement,
                    // but we may still need it (if learning abruptly terminates)
                    hypothesis = hypothesis.copy();
                    LOGGER.info("Refining hypothesis" + System.lineSeparator());

                    IOOracle ioOracle = this.stateFuzzerComposer.getSULOracle();
                    IOCounterexampleLoopRemover loops = new IOCounterexampleLoopRemover(ioOracle);
                    IOCounterExamplePrefixReplacer asrep = new IOCounterExamplePrefixReplacer(ioOracle);
                    IOCounterExamplePrefixFinder pref = new IOCounterExamplePrefixFinder(ioOracle);
                    counterExample = loops.optimizeCE(counterExample.getInput(),
                            (Hypothesis) hypothesis.getRegisterAutomaton());
                    counterExample = asrep.optimizeCE(counterExample.getInput(),
                            (Hypothesis) hypothesis.getRegisterAutomaton());
                    counterExample = pref.optimizeCE(counterExample.getInput(),
                            (Hypothesis) hypothesis.getRegisterAutomaton());

                    learner.addCounterexample(counterExample);
                    current_round++;
                }
            } while (counterExample != null);

            finished = true;

        } catch (TimeLimitReachedException e) {
            LOGGER.warn("Learning timed out after a duration of {} (i.e. {} hours, or {} minutes)",
                    e.getDuration(), e.getDuration().toHours(), e.getDuration().toMinutes());
            notFinishedReason = "time limit reached";

        } catch (TestLimitReachedException e) {
            LOGGER.warn("Learning exhausted the number of tests allowed ({} tests)", e.getTestLimit());
            notFinishedReason = "test limit reached";

        } catch (RoundLimitReachedException e) {
            LOGGER.info("Learning exhausted the number of hypothesis construction rounds allowed ({} rounds)",
                    e.getRoundLimit());
            notFinishedReason = "hypothesis construction round limit reached";

        } catch (Exception e) {
            notFinishedReason = e.getMessage();
            LOGGER.error("Exception generated during learning\n" + e);
            // useful to log what actually went wrong
            try (PrintWriter pw = new PrintWriter(
                    new FileWriter(new File(outputDir, ERROR_FILENAME), StandardCharsets.UTF_8))) {
                pw.println(e.getMessage());
                e.printStackTrace(pw);
            } catch (IOException exc) {
                LOGGER.error("Could not create error file writer");
            }
        }

        LOGGER.info("Finished Experiment");
        LOGGER.info("Number of refinement rounds: {}", current_round);
        LOGGER.info("Results stored in {}", outputDir.getPath());

        // TODO: Check the copy instead
        if (hypothesis == null) {
            LOGGER.info("Could not generate a first hypothesis, nothing to report on");
            if (notFinishedReason != null) {
                LOGGER.info("Potential cause: {}", notFinishedReason);
            }
            return learnerResult.toEmpty();
        }

        // building results

        learnerResult.setLearnedModel(hypothesis);
        learnerResult.setStateFuzzerEnabler(stateFuzzerEnabler);

        statisticsTracker.finishedLearning(hypothesis, finished, notFinishedReason);
        Statistics<I, Word<I>, Boolean, DefaultQuery<I, Boolean>> statistics = statisticsTracker
                .generateStatistics();
        learnerResult.setStatistics(statistics);
        LOGGER.info(statistics);

        // exporting to output files
        File learnedModelFile = new File(outputDir, LEARNED_MODEL_FILENAME);
        learnerResult.setLearnedModelFile(learnedModelFile);
        exportHypothesis(hypothesis, learnedModelFile);

        try {
            statistics.export(new FileWriter(new File(outputDir, STATISTICS_FILENAME), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Could not copy statistics to output directory");
        }

        return learnerResult;
    }

    /**
     * Copies the necessary files to the output directory.
     * <p>
     * Those files are the alphabet and the mapper connection configuration files.
     * Also if a test file is given for the SAMPLED_TESTS equivalence algorithm to
     * be used, then that test file is also copied to the output directory.
     *
     * @param outputDir the output directory, in which the files should be copied
     */
    protected void copyInputsToOutputDir(File outputDir) {
        try (InputStream inputStream = stateFuzzerComposer.getAlphabetFileInputStream()) {
            writeToFile(inputStream, new File(outputDir, ALPHABET_FILENAME));
        } catch (IOException e) {
            LOGGER.warn("Could not copy alphabet to output directory: " + e.getMessage());
        }

        if (stateFuzzerEnabler.getLearnerConfig().getEquivalenceAlgorithms()
                .contains(EquivalenceAlgorithmName.SAMPLED_TESTS)) {
            String testFile = stateFuzzerEnabler.getLearnerConfig().getTestFile();
            String testFilename = new File(testFile).getName();

            try (InputStream inputStream = new FileInputStream(testFile)) {
                writeToFile(inputStream, new File(outputDir, testFilename));
            } catch (IOException e) {
                LOGGER.warn("Could not copy sampled tests file to output directory: " + e.getMessage());
            }
        }

        try (InputStream inputStream = stateFuzzerEnabler.getSulConfig().getMapperConfig()
                .getMapperConnectionConfigInputStream()) {
            writeToFile(inputStream, new File(outputDir, MAPPER_CONNECTION_CONFIG_FILENAME));
        } catch (IOException e) {
            LOGGER.warn("Could not copy mapper connection config to output directory: " + e.getMessage());
        }
    }

    /**
     * Writes the contents of the input stream to the output file.
     *
     * @param inputStream the input stream of the source
     * @param outputFile  the output file of the destination
     *
     * @throws IOException if the reading or writing is not successful
     */
    protected void writeToFile(InputStream inputStream, File outputFile) throws IOException {
        if (inputStream == null) {
            throw new IOException("Null input stream due to possibly missing file");
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int bytesRead;
            byte[] byteArray = new byte[1024];
            while ((bytesRead = inputStream.read(byteArray, 0, byteArray.length)) > 0) {
                fileOutputStream.write(byteArray, 0, bytesRead);
            }
        }
    }

    /**
     * Returns a valid round limit number, which is either an integer or -1.
     *
     * @param roundLimit the integer to be converted, if it is needed
     * @return -1 if roundLimit is null or non-positive and roundLimit otherwise
     */
    protected int roundLimitToInt(Integer roundLimit) {
        if (roundLimit == null || roundLimit <= 0) {
            LOGGER.info("Learning round limit NOT set (provided value: {})", roundLimit);
            return -1;
        } else {
            LOGGER.info("Learning round limit set to {}", roundLimit);
            return roundLimit;
        }
    }

    /**
     * Exports a hypothesis to a file.
     *
     * @param hypothesis  the state machine hypothesis to be exported
     * @param destination the destination file
     */
    protected void exportHypothesis(RegisterAutomatonWrapper<I> hypothesis, File destination) {
        if (hypothesis == null) {
            LOGGER.warn("Provided null hypothesis to be exported");
            return;
        }
        hypothesis.export(destination);
    }
}
