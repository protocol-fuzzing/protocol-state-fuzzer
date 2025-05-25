package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DataWordSULWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.learnlib.sul.SUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DataWordSULWrapper;
import de.learnlib.ralib.words.OutputSymbol;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.MembershipOracleWrapper;
import net.automatalib.word.WordBuilder;
import java.util.stream.Stream;
/**
 * The standard implementation of the TestRunner Interface.
 *
 * @param <P>  the type of protocol messages
 * @param <E>  the type of execution context
 */
public class TestRunnerRA<I, P, E> implements TestRunner {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected TestRunnerEnabler testRunnerEnabler;

    /** The built alphabet using the AlphabetBuilder constructor parameter. */
    protected Alphabet<I> alphabet;


    /** Transformer to convert mealy input symbols into Ralib input symbols */
    protected AlphabetBuilderTransformer<I, ParameterizedSymbol> inputTransformer;

    /** The Mapper provided from the built {@link #sulOracle}. */
    protected Mapper<PSymbolInstance, PSymbolInstance, E> mapper;

    /** The Oracle that contains the sul built via SulBuilder and wrapped via SulWrapper constructor parameters. */
    protected SULOracle sulOracle;

    /** Stores the Mealy Machine specification built if provided in the TestRunnerConfig. */
    protected RegisterAutomaton testSpec;

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
    public TestRunnerRA(
        TestRunnerEnabler testRunnerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        AlphabetBuilderTransformer<I, ParameterizedSymbol> alphabetBuilderTransformer,
        SulBuilder<PSymbolInstance, PSymbolInstance, E> sulBuilder,
        SulWrapper<PSymbolInstance, PSymbolInstance, E> sulWrapper
    ) {
        this.testRunnerEnabler = testRunnerEnabler;
        this.alphabet = alphabetBuilder.build(
            testRunnerEnabler.getLearnerConfig()
        );
        this.cleanupTasks = new CleanupTasks();

        AbstractSul<PSymbolInstance, PSymbolInstance, E> abstractSul =
            sulBuilder.build(testRunnerEnabler.getSulConfig(), cleanupTasks);
        this.mapper = abstractSul.getMapper();
        SUL<PSymbolInstance, PSymbolInstance> sul = sulWrapper.wrap(abstractSul).getWrappedSul();

        this.sulOracle = new SULOracle(
            new DataWordSULWrapper(sul), new OutputSymbol("_io_err")
        );

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
    public TestRunnerRA<I, P, E> initialize() {
        if (
            this.testSpec == null &&
            this.testRunnerEnabler.getTestRunnerConfig()
                .getTestSpecification() !=
            null
        ) {
            throw new UnsupportedOperationException("Running with test spec is not implemented for RA learning.");
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
            List<
                TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>
            > results = runTests();

            for (TestRunnerResult<
                Word<PSymbolInstance>,
                Word<PSymbolInstance>
            > result : results) {
                LOGGER.info(result.toString());
                if (
                    testRunnerEnabler
                        .getTestRunnerConfig()
                        .isShowTransitionSequence()
                ) {
                    LOGGER.info(
                        "Displaying Transition Sequence\n{}", result
                    );
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
     * @throws IOException      if an error during reading occurs
     * @throws FormatException  if an invalid format was encountered
     */
    protected List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> runTests()
        throws IOException, FormatException {
        TestParser<I> testParser = new TestParser<>();
        List<Word<I>> tests;
        String testFileOrTestString = testRunnerEnabler
            .getTestRunnerConfig()
            .getTest();

        if (new File(testFileOrTestString).exists()) {
            tests = testParser.readTests(alphabet, testFileOrTestString);
        } else {
            LOGGER.info(
                "File {} does not exist, interpreting argument as test",
                testFileOrTestString
            );
            String[] testStrings = testFileOrTestString.split("\\s+");
            tests = List.of(
                testParser.readTest(alphabet, Arrays.asList(testStrings))
            );
        }

        // net.automatalib.word.WordCollector<I> exists but is not explicitly marked pulic.
        // This is most likely unintended since it is a wrapper around WordBuilder which is public.
        // Using that would allow us to skip using WordBuilder directly.
        // TODO: Open an issue or otherwise notify about this.
        WordBuilder<PSymbolInstance> wordBuilder = new WordBuilder<>();
        List<Word<PSymbolInstance>> convertedTests = new ArrayList<>(tests.size());
        for (Word<I> test : tests) {
            List<PSymbolInstance> wordList = test.stream()
                                                  .map(inputTransformer::toTransformedInput)
                                                  .map(p -> new PSymbolInstance(p))
                                                  .toList();
            Word<PSymbolInstance> pWord = wordBuilder.append(wordList).toWord();
            convertedTests.add(pWord);
            wordBuilder.clear();

        }


        List<
            TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>
        > results = new ArrayList<>();
        for (Word<PSymbolInstance> test : convertedTests) {
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
    protected TestRunnerResult<
        Word<PSymbolInstance>,
        Word<PSymbolInstance>
    > runTest(Word<PSymbolInstance> test) {
        TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>> result =
            TestRunner.runTest(
                test,
                testRunnerEnabler.getTestRunnerConfig().getTimes(),
                new MembershipOracleWrapper(sulOracle)
            );
        return result;
    }
}
