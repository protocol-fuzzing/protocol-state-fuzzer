package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.learnlib.sul.SUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.MembershipOracleWrapperRA;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DataWordSULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;
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
 * @param <I> the type of input parameters
 * @param <E> the type of execution context
 */
public class TestRunnerRA<I, E> implements TestRunner {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected TestRunnerEnabler testRunnerEnabler;

    /** The built alphabet using the AlphabetBuilder constructor parameter. */
    protected Alphabet<I> alphabet;

    /** Transformer to convert mealy input symbols into Ralib input symbols. */
    protected AlphabetBuilderTransformer<I, ParameterizedSymbol> inputTransformer;

    /**
     * The Oracle that contains the SUL built via SULBuilder and wrapped via SULWrapper constructor
     * parameters.
     */
    protected SULOracle sulOracle;

    /** Stores the cleanup tasks of the TestRunner. */
    protected CleanupTasks cleanupTasks;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * The {@link #sulOracle} contains the wrapped (and built) SUL.
     * Invoke {@link #initialize()} afterwards.
     *
     * @param testRunnerEnabler          the configuration that enables the testing
     * @param alphabetBuilder            the builder of the alphabet
     * @param alphabetBuilderTransformer the transformer used to translate inputs
     * @param sulBuilder                 the builder of the SUL
     */
    public TestRunnerRA(
        TestRunnerEnabler testRunnerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        AlphabetBuilderTransformer<I, ParameterizedSymbol> alphabetBuilderTransformer,
        SULBuilder<PSymbolInstance, PSymbolInstance, E> sulBuilder) {
        this.testRunnerEnabler = testRunnerEnabler;
        this.alphabet = alphabetBuilder.build(testRunnerEnabler.getLearnerConfig());
        this.inputTransformer = alphabetBuilderTransformer;
        this.cleanupTasks = new CleanupTasks();

        AbstractSUL<PSymbolInstance, PSymbolInstance, E> abstractSUL = sulBuilder
            .buildSUL(testRunnerEnabler.getSULConfig(), cleanupTasks);
        SULWrapper<PSymbolInstance, PSymbolInstance, E> sulWrapper = sulBuilder.buildWrapper();
        SUL<PSymbolInstance, PSymbolInstance> sul = sulWrapper.wrap(abstractSUL).getWrappedSUL();

        this.sulOracle = new SULOracle(
            new DataWordSULWrapper(sul), new OutputSymbol("_io_err"));
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * <p>
     * It checks if the TestRunnerConfig from the TestRunnerEnabler contains
     * any test specification that needs to be built and used.
     *
     * @return the same instance
     */
    public TestRunnerRA<I, E> initialize() {
        if (this.testRunnerEnabler.getTestRunnerConfig().getTestSpecification() != null) {
            throw new UnsupportedOperationException("Running with test spec is not implemented for RA learning.");
        }
        return this;
    }

    /**
     * Returns the alphabet to be used during testing.
     *
     * @return the alphabet to be used during testing
     */
    public Alphabet<I> getAlphabet() {
        return alphabet;
    }

    /**
     * Returns the SULConfig of the {@link #testRunnerEnabler}.
     *
     * @return the SULConfig of the {@link #testRunnerEnabler}
     */
    public SULConfig getSULConfig() {
        return testRunnerEnabler.getSULConfig();
    }

    /**
     * Runs the tests using {@link #runTests()} and cleans up using {@link #terminate()}.
     */
    @Override
    public void run() {
        try {
            List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> results = runTests();

            for (TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>> result: results) {
                LOGGER.info(result.toString());
                if (testRunnerEnabler.getTestRunnerConfig().isShowTransitionSequence()) {
                    LOGGER.info("Displaying Transition Sequence\n{}", result);
                }
            }
        }
        catch (IOException | FormatException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
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
     * @return                 a list with the test results
     *
     * @throws IOException     if an error during reading occurs
     * @throws FormatException if an invalid format was encountered
     */
    protected List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> runTests()
        throws IOException, FormatException {
        TestParserRA testParser = new TestParserRA();
        List<Word<PSymbolInstance>> tests;
        String testFileOrTestString = testRunnerEnabler
            .getTestRunnerConfig()
            .getTest();

        ListAlphabet<InputSymbol> inputSymbolAlphabet = new ListAlphabet<>(alphabet.stream()
            .map(inputTransformer::toTransformedInput)
            .filter(i -> i instanceof InputSymbol)
            .map(i -> (InputSymbol) i).toList());

        if (new File(testFileOrTestString).exists()) {
            tests = testParser.readTests(inputSymbolAlphabet, testFileOrTestString);
        } else {
            LOGGER.info(
                "File {} does not exist, interpreting argument as test",
                testFileOrTestString);
            String[] testStrings = testFileOrTestString.split("\\s+");
            tests = List.of(
                testParser.readTest(inputSymbolAlphabet, Arrays.asList(testStrings)));
        }

        List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> results = new ArrayList<>();
        for (Word<PSymbolInstance> test: tests) {
            results.add(runTest(test));
        }
        return results;
    }

    /**
     * Runs a single test and collects the result.
     *
     * @param  test the test to be run against the stored {@link #sulOracle}
     *
     * @return      the result of the test
     */
    protected TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>> runTest(Word<PSymbolInstance> test) {
        TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>> result = TestRunner.runTest(
            test,
            testRunnerEnabler.getTestRunnerConfig().getTimes(),
            new MembershipOracleWrapperRA(sulOracle));
        return result;
    }
}
