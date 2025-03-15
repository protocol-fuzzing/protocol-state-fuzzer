package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The standard implementation of the TestRunner Interface.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 * @param <E>  the type of execution context
 */
public class TestRunnerStandard<I, O extends MapperOutput<O, P>, P, E> implements TestRunner {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected TestRunnerEnabler testRunnerEnabler;

    /** The built alphabet using the AlphabetBuilder constructor parameter. */
    protected Alphabet<I> alphabet;

    /** The Mapper provided from the built {@link #sulOracle}. */
    protected Mapper<I, O, E> mapper;

    /** The Oracle that contains the sul built via SulBuilder and wrapped via SulWrapper constructor parameters. */
    protected MealyMembershipOracle<I, O> sulOracle;

    /** Stores the Mealy Machine specification built if provided in the TestRunnerConfig. */
    protected MealyMachine<?, I, ?, O> testSpec;

    /** Stores the cleanup tasks of the TestRunner. */
    protected CleanupTasks cleanupTasks;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * The {@link #sulOracle} contains the wrapped (and built) sul.
     * Invoke {@link #initialize()} afterwards.
     *
     * @param testRunnerEnabler        the configuration that enables the testing
     * @param alphabetBuilder          the builder of the alphabet
     * @param sulBuilder               the builder of the sul
     * @param sulWrapper               the wrapper of the sul
     */
    public TestRunnerStandard(
        TestRunnerEnabler testRunnerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SulBuilder<I, O, E> sulBuilder,
        SulWrapper<I, O, E> sulWrapper
    ) {
        this.testRunnerEnabler = testRunnerEnabler;
        this.alphabet = alphabetBuilder.build(testRunnerEnabler.getLearnerConfig());
        this.cleanupTasks = new CleanupTasks();

        AbstractSul<I, O, E> abstractSul = sulBuilder.build(testRunnerEnabler.getSulConfig(), cleanupTasks);
        this.mapper = abstractSul.getMapper();
        this.sulOracle = new SULOracle<>(sulWrapper.wrap(abstractSul).getWrappedSul());

        this.testSpec = null;
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * <p>
     * It checks if the TestRunnerConfig from the TestRunnerEnabler contains
     * any test specification that needs to be built and used.
     *
     * @return  the same instance
     */
    public TestRunnerStandard<I, O, P, E> initialize() {
        if (this.testSpec == null &&
            this.testRunnerEnabler.getTestRunnerConfig().getTestSpecification() != null) {

            try {
                this.testSpec = ModelFactory.buildProtocolModel(
                    testRunnerEnabler.getTestRunnerConfig().getTestSpecification(),
                    new MealyIOProcessor<>(alphabet, mapper.getOutputBuilder())
                );

            } catch (IOException | FormatException e) {
                throw new RuntimeException("Could not build protocol model from test specification: " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * Returns the alphabet to be used during testing.
     *
     * @return  the alphabet to be used during testing
     */
    public Alphabet<I> getAlphabet() {
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
    @Override
    public void run() {
        try {
            List<TestRunnerResult<Word<I>, Word<O>>> results = runTests();

            for (TestRunnerResult<Word<I>, Word<O>> result : results) {
                LOGGER.info(result.toString());
                if (testRunnerEnabler.getTestRunnerConfig().isShowTransitionSequence()) {
                    LOGGER.info("Displaying Transition Sequence\n{}", getTransitionSequenceString(result));
                }
            }
        } catch (IOException | FormatException e) {
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
    protected List<TestRunnerResult<Word<I>, Word<O>>> runTests() throws IOException, FormatException {
        TestParser<I> testParser = new TestParser<>();
        List<Word<I>> tests;
        String testFileOrTestString = testRunnerEnabler.getTestRunnerConfig().getTest();

        if (new File(testFileOrTestString).exists()) {
            tests = testParser.readTests(alphabet, testFileOrTestString);
        } else {
            LOGGER.info("File {} does not exist, interpreting argument as test", testFileOrTestString);
            String[] testStrings = testFileOrTestString.split("\\s+");
            tests = List.of(testParser.readTest(alphabet, Arrays.asList(testStrings)));
        }

        List<TestRunnerResult<Word<I>, Word<O>>> results = new ArrayList<>();
        for (Word<I> test : tests) {
            results.add(runTest(test));
        }
        return results;
    }

    /**
     * Runs a single test and collects the result.
     * <p>
     * If a {@link #testSpec} is present then its output to the provided test
     * is computed and stored also in the TestRunnerResult as the expected output.
     *
     * @param test  the test to be run against the stored {@link #sulOracle}
     * @return      the result of the test
     */
    protected TestRunnerResult<Word<I>, Word<O>> runTest(Word<I> test) {
        TestRunnerResult<Word<I>, Word<O>> result = TestRunner.runTest(test,
                testRunnerEnabler.getTestRunnerConfig().getTimes(), sulOracle);

        if (testSpec != null) {
            Word<O> outputWord = testSpec.computeOutput(test);
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
    protected String getTransitionSequenceString(TestRunnerResult<Word<I>, Word<O>> result) {

        StringBuilder sb = new StringBuilder();

        for (Word<O> answer : result.getGeneratedOutputs().keySet()) {
            sb.append(System.lineSeparator());

            for (int i = 0; i < result.getInputWord().size(); i++) {
                List<O> atomicOutputs = new ArrayList<O>(answer.getSymbol(i).getAtomicOutputs(2));

                if (getSulConfig().isFuzzingClient()
                     && i == 0
                     && mapper.getOutputChecker().hasInitialClientMessage(atomicOutputs.get(0))) {

                    sb.append("- / ").append(atomicOutputs.get(0)).append(System.lineSeparator());
                    atomicOutputs.remove(0);
                }

                sb.append(result.getInputWord().getSymbol(i)).append(" / ");

                if (atomicOutputs.isEmpty() || mapper.getOutputChecker().isTimeout(answer.getSymbol(i))) {
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
