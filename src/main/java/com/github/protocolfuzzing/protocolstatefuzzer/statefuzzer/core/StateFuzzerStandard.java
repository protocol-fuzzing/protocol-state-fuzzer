package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.StateMachine;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.RoundLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TestLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TimeLimitReachedException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.Statistics;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * The standard implementation of the StateFuzzer Interface.
 */
public class StateFuzzerStandard implements StateFuzzer {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The filename of the alphabet with the extension from {@link #stateFuzzerComposer}. */
    protected final String ALPHABET_FILENAME;

    /** Stores the constructor parameter. */
    protected StateFuzzerComposer stateFuzzerComposer;

    /** The alphabet from the {@link #stateFuzzerComposer}. */
    protected Alphabet<AbstractInput> alphabet;

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
    public StateFuzzerStandard(StateFuzzerComposer stateFuzzerComposer) {
        this.stateFuzzerComposer = stateFuzzerComposer;
        this.stateFuzzerEnabler = stateFuzzerComposer.getStateFuzzerEnabler();
        this.alphabet = stateFuzzerComposer.getAlphabet();
        this.outputDir = stateFuzzerComposer.getOutputDir();
        this.cleanupTasks = stateFuzzerComposer.getCleanupTasks();
        this.ALPHABET_FILENAME = ALPHABET_FILENAME_NO_EXTENSION + stateFuzzerComposer.getAlphabetFileExtension();
    }

    @Override
    public void startFuzzing() {
        try {
            inferStateMachine();
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
     */
    protected void inferStateMachine() {
        // for convenience, we copy all the input files/streams
        // to the output directory before starting the arduous learning process
        copyInputsToOutputDir(outputDir);

        // setting up statistics tracker, learner and equivalence oracle
        StatisticsTracker statisticsTracker = stateFuzzerComposer.getStatisticsTracker();

        MealyLearner<AbstractInput, AbstractOutput> learner = stateFuzzerComposer.getLearner();

        EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>>
                equivalenceOracle = stateFuzzerComposer.getEquivalenceOracle();

        MealyMachine<?, AbstractInput, ?, AbstractOutput> hypothesis;
        StateMachine stateMachine = null;
        DefaultQuery<AbstractInput, Word<AbstractOutput>> counterExample;
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
            LOGGER.info("Starting Learning");
            statisticsTracker.startLearning(stateFuzzerEnabler, alphabet);
            learner.startLearning();
            current_round++;

            do {
                hypothesis = learner.getHypothesisModel();
                stateMachine = new StateMachine(hypothesis, alphabet);
                // it is useful to print intermediate hypothesis as learning is running
                String hypName = "hyp" + current_round + ".dot";
                exportHypothesis(stateMachine, outputDir, hypName, false);
                statisticsTracker.newHypothesis(stateMachine);
                LOGGER.info("Generated new hypothesis: " + hypName);

                if (current_round == round_limit) {
                    // round_limit can be either -1 (no limit) or a positive int
                    throw new RoundLimitReachedException(round_limit);
                }

                LOGGER.info("Validating hypothesis");
                counterExample = equivalenceOracle.findCounterExample(hypothesis, alphabet);

                if (counterExample != null) {
                    LOGGER.warn("Counterexample: " + counterExample);
                    statisticsTracker.newCounterExample(counterExample);
                    // we create a copy, since the hypothesis reference will not be valid after refinement,
                    // but we may still need it (if learning abruptly terminates)
                    stateMachine = stateMachine.copy();
                    LOGGER.info("Refining hypothesis");
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
            LOGGER.error("Exception generated during learning%n" + e);
            // useful to log what actually went wrong
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(outputDir, ERROR_FILENAME)))) {
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
            return;
        }

        // building results
        statisticsTracker.finishedLearning(stateMachine, finished, notFinishedReason);
        Statistics statistics = statisticsTracker.generateStatistics();
        LOGGER.info(statistics);

        // exporting to output files
        exportHypothesis(stateMachine, outputDir, LEARNED_MODEL_FILENAME, true);

        try {
            statistics.export(new FileWriter(new File(outputDir, STATISTICS_FILENAME)));
        } catch (IOException e) {
            LOGGER.error("Could not copy statistics to output directory");
        }
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
            LOGGER.error("Could not copy alphabet to output directory");
            e.printStackTrace();
        }

        if (stateFuzzerEnabler.getLearnerConfig().getEquivalenceAlgorithms().contains(EquivalenceAlgorithmName.SAMPLED_TESTS)) {
            String testFile = stateFuzzerEnabler.getLearnerConfig().getTestFile();
            String testFilename = new File(testFile).getName();

            try (InputStream inputStream = new FileInputStream(testFile)) {
                writeToFile(inputStream, new File(outputDir, testFilename));
            } catch (IOException e) {
                LOGGER.error("Could not copy sampled tests file to output directory");
                e.printStackTrace();
            }
        }

        try (InputStream inputStream = stateFuzzerEnabler.getSulConfig().getMapperConfig().getMapperConnectionConfigInputStream()) {
            writeToFile(inputStream, new File(outputDir, MAPPER_CONNECTION_CONFIG_FILENAME));
        } catch (IOException e) {
            LOGGER.error("Could not copy mapper connection config to output directory");
            e.printStackTrace();
        }
    }

    /**
     * Writes the contents of the input stream to the output file.
     *
     * @param inputStream   the input stream of the source
     * @param outputFile    the output file of the destination
     * @throws IOException  if the reading/writing is not successful
     */
    protected void writeToFile(InputStream inputStream, File outputFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int bytesRead;
            byte[] byteArray = new byte[1024];
            while ((bytesRead = inputStream.read(byteArray)) > 0) {
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
     * @param hypothesis  the state machine hypothesis to be exported
     * @param dir         the output directory of the file
     * @param name        the name of the exported file
     * @param genPdf      {@code true} if the hypothesis needs to be exported
     *                    also in a pdf format
     */
    protected void exportHypothesis(StateMachine hypothesis, File dir, String name, boolean genPdf) {
        if (hypothesis != null) {
            File graphFile = new File(dir, name);
            hypothesis.export(graphFile, genPdf);
        } else {
            LOGGER.info("Provided null hypothesis to be serialized");
        }
    }
}
