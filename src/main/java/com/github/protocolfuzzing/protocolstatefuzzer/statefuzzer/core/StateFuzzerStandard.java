package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.StateMachine;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.RoundLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TestLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TimeLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.Statistics;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * The standard implementation of the StateFuzzer Interface.
 */
public class StateFuzzerStandard<I, O extends MapperOutput<O, P>, P> implements StateFuzzer<I, O> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The filename of the alphabet with the extension from {@link #stateFuzzerComposer}. */
    protected final String ALPHABET_FILENAME;

    /** Stores the constructor parameter. */
    protected StateFuzzerComposer<I, O> stateFuzzerComposer;

    /** The alphabet from the {@link #stateFuzzerComposer}. */
    protected Alphabet<I> alphabet;

    /** The output directory from the {@link #stateFuzzerComposer}. */
    protected File outputDir;

    /** The cleanup tasks from the {@link #stateFuzzerComposer}. */
    protected CleanupTasks cleanupTasks;

    /** The StateFuzzerEnabler from the {@link #stateFuzzerComposer}. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param stateFuzzerComposer  contains the learning components to be used
     *                             for the state fuzzing
     */
    public StateFuzzerStandard(StateFuzzerComposer<I, O> stateFuzzerComposer) {
        this.stateFuzzerComposer = stateFuzzerComposer;
        this.stateFuzzerEnabler = stateFuzzerComposer.getStateFuzzerEnabler();
        this.alphabet = stateFuzzerComposer.getAlphabet();
        this.outputDir = stateFuzzerComposer.getOutputDir();
        this.cleanupTasks = stateFuzzerComposer.getCleanupTasks();
        this.ALPHABET_FILENAME = ALPHABET_FILENAME_NO_EXTENSION + stateFuzzerComposer.getAlphabetFileExtension();
    }

    @Override
    public LearnerResult<I, O> startFuzzing() {
        try {
            return inferStateMachine();
        } catch (RuntimeException e) {
            LOGGER.error("Exception encountered during state fuzzing");
            throw e;
        } finally {
            cleanupTasks.execute();
        }
    }


    /**
     * Uses the learning components for the state fuzzing.
     * <p>
     * Also it copies the necessary files, proceeds with the state fuzzing and
     * exports the final statistics.
     *
     * @return  the corresponding LearnerResult, which can be empty if state
     *          fuzzing fails
     */
    protected LearnerResult<I, O> inferStateMachine() {
        // for convenience, we copy all the input files/streams
        // to the output directory before starting the arduous learning process
        copyInputsToOutputDir(outputDir);

        // setting up statistics tracker, learner and equivalence oracle
        StatisticsTracker<I, O> statisticsTracker = stateFuzzerComposer.getStatisticsTracker();

        MealyLearner<I, O> learner = stateFuzzerComposer.getLearner();

        EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
                equivalenceOracle = stateFuzzerComposer.getEquivalenceOracle();

        MealyMachine<?, I, ?, O> hypothesis;
        StateMachine<I, O> stateMachine = null;
        LearnerResult<I, O> learnerResult = new LearnerResult<>();
        DefaultQuery<I, Word<O>> counterExample;
        boolean finished = false;
        String notFinishedReason = null;
        int current_round = 0;
        int round_limit = roundLimitToInt(stateFuzzerEnabler.getLearnerConfig().getRoundLimit());

        try {
            statisticsTracker.setRuntimeStateTracking(new FileOutputStream(
                    new File(outputDir, LEARNING_STATE_FILENAME)));
        } catch (FileNotFoundException e1) {
            throw new RuntimeException("Could not create runtime state tracking output stream");
        }

        try {
            LOGGER.info("Input alphabet: {}", alphabet);
            LOGGER.info("Starting Learning" + System.lineSeparator());
            statisticsTracker.startLearning(stateFuzzerEnabler, alphabet);
            learner.startLearning();
            current_round++;

            do {
                hypothesis = learner.getHypothesisModel();
                stateMachine = new StateMachine<>(hypothesis, alphabet);
                learnerResult.addHypothesis(stateMachine);
                // it is useful to print intermediate hypothesis as learning is running
                String hypName = "hyp" + current_round + ".dot";
                exportHypothesis(stateMachine, new File(outputDir, hypName));
                statisticsTracker.newHypothesis(stateMachine);
                LOGGER.info("Generated new hypothesis: " + hypName);

                if (current_round == round_limit) {
                    // round_limit can be either -1 (no limit) or a positive int
                    throw new RoundLimitReachedException(round_limit);
                }

                LOGGER.info("Validating hypothesis" + System.lineSeparator());
                counterExample = equivalenceOracle.findCounterExample(hypothesis, alphabet);

                if (counterExample != null) {
                    LOGGER.info("Counterexample: " + counterExample);
                    statisticsTracker.newCounterExample(counterExample);
                    // we create a copy, since the hypothesis reference will not be valid after refinement,
                    // but we may still need it (if learning abruptly terminates)
                    stateMachine = stateMachine.copy();
                    LOGGER.info("Refining hypothesis" + System.lineSeparator());
                    learner.refineHypothesis(counterExample);
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
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(outputDir, ERROR_FILENAME), StandardCharsets.UTF_8))) {
                pw.println(e.getMessage());
                e.printStackTrace(pw);
            } catch (IOException exc) {
                LOGGER.error("Could not create error file writer");
            }
        }

        LOGGER.info("Finished Experiment");
        LOGGER.info("Number of refinement rounds: {}", current_round);
        LOGGER.info("Results stored in {}", outputDir.getPath());

        if (stateMachine == null) {
            LOGGER.info("Could not generate a first hypothesis, nothing to report on");
            if (notFinishedReason != null) {
                LOGGER.info("Potential cause: {}", notFinishedReason);
            }
            return learnerResult.toEmpty();
        }

        // building results
        learnerResult.setLearnedModel(stateMachine);
        learnerResult.setStateFuzzerEnabler(stateFuzzerEnabler);

        statisticsTracker.finishedLearning(stateMachine, finished, notFinishedReason);
        Statistics<I, O> statistics = statisticsTracker.generateStatistics();
        learnerResult.setStatistics(statistics);
        LOGGER.info(statistics);


        // exporting to output files
        File learnedModelFile = new File(outputDir, LEARNED_MODEL_FILENAME);
        learnerResult.setLearnedModelFile(learnedModelFile);
        exportHypothesis(stateMachine, learnedModelFile);

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
     * @param outputDir  the output directory, in which the files should be copied
     */
    protected void copyInputsToOutputDir(File outputDir) {
        try (InputStream inputStream = stateFuzzerComposer.getAlphabetFileInputStream()) {
            writeToFile(inputStream, new File(outputDir, ALPHABET_FILENAME));
        } catch (IOException e) {
            LOGGER.warn("Could not copy alphabet to output directory: " + e.getMessage());
        }

        if (stateFuzzerEnabler.getLearnerConfig().getEquivalenceAlgorithms().contains(EquivalenceAlgorithmName.SAMPLED_TESTS)) {
            String testFile = stateFuzzerEnabler.getLearnerConfig().getTestFile();
            String testFilename = new File(testFile).getName();

            try (InputStream inputStream = new FileInputStream(testFile)) {
                writeToFile(inputStream, new File(outputDir, testFilename));
            } catch (IOException e) {
                LOGGER.warn("Could not copy sampled tests file to output directory: " + e.getMessage());
            }
        }

        try (InputStream inputStream = stateFuzzerEnabler.getSulConfig().getMapperConfig().getMapperConnectionConfigInputStream()) {
            writeToFile(inputStream, new File(outputDir, MAPPER_CONNECTION_CONFIG_FILENAME));
        } catch (IOException e) {
            LOGGER.warn("Could not copy mapper connection config to output directory: " + e.getMessage());
        }
    }

    /**
     * Writes the contents of the input stream to the output file.
     *
     * @param inputStream   the input stream of the source
     * @param outputFile    the output file of the destination
     *
     * @throws IOException  if the reading or writing is not successful
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
     * @param roundLimit  the integer to be converted, if it is needed
     * @return            -1 if roundLimit is null or non-positive and roundLimit otherwise
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
     * @param hypothesis   the state machine hypothesis to be exported
     * @param destination  the destination file
     */
    protected void exportHypothesis(StateMachine<I, O> hypothesis, File destination) {
        if (hypothesis == null) {
            LOGGER.warn("Provided null hypothesis to be exported");
            return;
        }
        hypothesis.export(destination);
    }
}
