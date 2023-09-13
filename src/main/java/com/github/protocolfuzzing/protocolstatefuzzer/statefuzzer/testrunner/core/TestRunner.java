package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * It is responsible for the testing process.
 */
public class TestRunner {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected TestRunnerEnabler testRunnerEnabler;

    /** The built alphabet using the AlphabetBuilder constructor parameter. */
    protected Alphabet<AbstractInput> alphabet;

    /** The Mapper provided from the built {@link #sulOracle}. */
    protected Mapper mapper;

    /** The Oracle that contains the sul built via SulBuilder and wrapped via SulWrapper constructor parameters. */
    protected MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle;

    /** Stores the Mealy Machine specification built if provided in the TestRunnerConfig. */
    protected MealyMachine<?, AbstractInput, ?, AbstractOutput> testSpecification;

    /** Stores the cleanup tasks of the TestRunner. */
    protected CleanupTasks cleanupTasks;

    /**
     * Runs a single test multiple times against a specified sulOracle.
     *
     * @param <I>        the type of inputs
     * @param <O>        the type of outputs
     * @param test       the test to be run in an input word format
     * @param times      the number of times to repeat the test
     * @param sulOracle  the Oracle against which the test will be run
     * @return           the corresponding {@link TestRunnerResult}
     */
    public static <I, O> TestRunnerResult<I, O> runTest(Word<I> test, int times, MealyMembershipOracle<I, O> sulOracle) {
        LinkedHashMap<Word<O>, Integer> answerMap = new LinkedHashMap<>();
        for (int i = 0; i < times; i++) {
            Word<O> answer = sulOracle.answerQuery(test);
            if (!answerMap.containsKey(answer)) {
                answerMap.put(answer, 1);
            } else {
                answerMap.put(answer, answerMap.get(answer) + 1);
            }
        }
        return new TestRunnerResult<>(test, answerMap);
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * It also checks if the TestRunnerConfig from the TestRunnerEnabler contains
     * any test specification that needs to be built and used.
     * The {@link #sulOracle} contains the wrapped (and built) sul.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @param alphabetBuilder    the builder of the alphabet
     * @param sulBuilder         the builder of the sul
     * @param sulWrapper         the wrapper of the sul
     */
    public TestRunner(TestRunnerEnabler testRunnerEnabler, AlphabetBuilder alphabetBuilder,
        SulBuilder sulBuilder, SulWrapper sulWrapper) {

        this.testRunnerEnabler = testRunnerEnabler;
        this.alphabet = alphabetBuilder.build(testRunnerEnabler.getLearnerConfig());
        this.cleanupTasks = new CleanupTasks();

        AbstractSul abstractSul = sulBuilder.build(testRunnerEnabler.getSulConfig(), cleanupTasks);
        this.mapper = abstractSul.getMapper();
        this.sulOracle = new SULOracle<>(sulWrapper.wrap(abstractSul).getWrappedSul());

        this.testSpecification = null;
        if (testRunnerEnabler.getTestRunnerConfig().getTestSpecification() != null) {
            try {
                this.testSpecification = ModelFactory.buildProtocolModel(
                        alphabet, testRunnerEnabler.getTestRunnerConfig().getTestSpecification());
            } catch (IOException e) {
                throw new RuntimeException("Could not build protocol model from test specification: " + e.getMessage());
            }
        }

    }

    /**
     * Returns the alphabet to be used during testing.
     *
     * @return  the alphabet to be used during testing
     */
    public Alphabet<AbstractInput> getAlphabet() {
        return alphabet;
    }

    /**
     * Returns the SulConfig of the {@link #testRunnerEnabler}.
     *
     * @return  the SulConfig of the {@link #testRunnerEnabler}
     */
    public SulConfig getSulConfig() {
        return testRunnerEnabler.getSulConfig();
    }

    /**
     * Runs the tests using {@link #runTests()} and cleans up using {@link #terminate()}.
     */
    public void run() {
        try {
            List<TestRunnerResult<AbstractInput, AbstractOutput>> results = runTests();

            for (TestRunnerResult<AbstractInput, AbstractOutput> result : results) {
                LOGGER.info(result.toString());
                if (testRunnerEnabler.getTestRunnerConfig().isShowTransitionSequence()) {
                    LOGGER.info("Displaying Transition Sequence\n{}", getTransitionSequenceString(result));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } finally {
            terminate();
        }
    }

    /**
     * Executes the {@link #cleanupTasks}; should be called only after all the
     * desired tests have been executed.
     */
    public void terminate() {
        cleanupTasks.execute();
    }

    /**
     * Reads the tests provided in the TestRunnerConfig of {@link #testRunnerEnabler},
     * executes each one of them using {@link #runTest(Word)} and collects the results.
     *
     * @return  a list with the test results
     *
     * @throws IOException  if an error during reading occurs
     */
    protected List<TestRunnerResult<AbstractInput, AbstractOutput>> runTests() throws IOException {
        TestParser testParser = new TestParser();
        List<Word<AbstractInput>> tests;
        String testFileOrTestString = testRunnerEnabler.getTestRunnerConfig().getTest();

        if (new File(testFileOrTestString).exists()) {
            tests = testParser.readTests(alphabet, testFileOrTestString);
        } else {
            LOGGER.info("File {} does not exist, interpreting argument as test", testFileOrTestString);
            String[] testStrings = testFileOrTestString.split("\\s+");
            tests = List.of(testParser.readTest(alphabet, Arrays.asList(testStrings)));
        }

        List<TestRunnerResult<AbstractInput, AbstractOutput>> results = new ArrayList<>();
        for (Word<AbstractInput> test : tests) {
            TestRunnerResult<AbstractInput, AbstractOutput> result = runTest(test);
            results.add(result);
        }
        return results;
    }

    /**
     * Runs a single test and collects the result.
     * <p>
     * If a {@link #testSpecification} is present then its output to the provided test
     * is computed and stored also in the TestRunnerResult as the expected output.
     *
     * @param test  the test to be run against the stored {@link #sulOracle}
     * @return      the result of the test
     */
    protected TestRunnerResult<AbstractInput, AbstractOutput> runTest(Word<AbstractInput> test) {
        TestRunnerResult<AbstractInput, AbstractOutput> result = TestRunner.runTest(test,
                testRunnerEnabler.getTestRunnerConfig().getTimes(), sulOracle);

        if (testSpecification != null) {
            Word<AbstractOutput> outputWord = testSpecification.computeOutput(test);
            result.setExpectedOutputWord(outputWord);
        }
        return result;
    }

    /**
     * Returns a nice representation of the corresponding input and outputs
     * obtained from the result of a test run.
     *
     * @param result  the test run result to be read
     * @return        the transition sequence string
     */
    protected String getTransitionSequenceString(TestRunnerResult<AbstractInput, AbstractOutput> result) {

        StringBuilder sb = new StringBuilder();

        for (Word<AbstractOutput> answer : result.getGeneratedOutputs().keySet()) {
            sb.append(System.lineSeparator());

            for (int i = 0; i < result.getInputWord().size(); i++) {
                List<AbstractOutput> atomicOutputs = new ArrayList<>(answer.getSymbol(i).getAtomicOutputs(2));

                if (getSulConfig().isFuzzingClient()
                     && i == 0
                     && mapper.getAbstractOutputChecker().hasInitialClientMessage(atomicOutputs.get(0))) {

                    sb.append("- / ").append(atomicOutputs.get(0)).append(System.lineSeparator());
                    atomicOutputs.remove(0);
                }

                sb.append(result.getInputWord().getSymbol(i)).append(" / ");

                if (answer.getSymbol(i).isTimeout() || atomicOutputs.isEmpty()) {
                    sb.append("-");
                } else {
                    atomicOutputs.forEach(ao -> sb.append(ao).append("; "));
                    sb.deleteCharAt(sb.length() - 2);
                }

                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
