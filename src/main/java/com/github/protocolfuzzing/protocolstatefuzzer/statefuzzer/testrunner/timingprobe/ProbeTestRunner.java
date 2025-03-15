package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TestRunnerStandard extended to be used by the TimingProbe.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 * @param <E>  the type of execution context
 */
public class ProbeTestRunner<I, O extends MapperOutput<O, P>, P, E> extends TestRunnerStandard<I, O, P, E>  {

    /** Stores a list of results. */
    protected List<TestRunnerResult<Word<I>, Word<O>>> cachedResults = null;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @param alphabetBuilder    the builder of the alphabet
     * @param sulBuilder         the builder of the sul
     * @param sulWrapper         the wrapper of the sul
     */
    public ProbeTestRunner(
        TestRunnerEnabler testRunnerEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SulBuilder<I, O, E> sulBuilder,
        SulWrapper<I, O, E> sulWrapper
    ) {
        super(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    /**
     * Runs the current tests using {@link #runTests()} and checks if they are not deterministic.
     * <p>
     * If there are {@link #cachedResults}, then any new result should correspond
     * to a cached one, with which should have the same outputs.
     *
     * @param cacheFoundResults  cache the found results to {@link #cachedResults}
     * @return                   {@code true} if any result is found to be
     *                           non-deterministic
     *
     * @throws IOException       if an error occurs during {@link #runTests()}
     */
    public boolean isNonDeterministic(boolean cacheFoundResults) throws IOException, FormatException {
        List<TestRunnerResult<Word<I>, Word<O>>> results = super.runTests();
        Iterator<TestRunnerResult<Word<I>, Word<O>>> iterator = null;

        if (cachedResults != null) {
            iterator = cachedResults.iterator();
        }

        for (TestRunnerResult<Word<I>, Word<O>> result : results) {
            Map<Word<O>, Integer> resultOutputs = result.getGeneratedOutputs();

            if (resultOutputs == null) {
                throw new NullPointerException("Null output map provided");
            }

            // non-deterministic test if there are many different outputs
            if (resultOutputs.size() > 1) {
                return true;
            }

            if (iterator != null) {
                // every new result should have a corresponding cached result
                if (!iterator.hasNext()) {
                    return true;
                }

                Map<Word<O>, Integer> expectedOutputs = iterator.next().getGeneratedOutputs();

                // non-deterministic test if the new results are different from the cached ones
                if (!resultOutputs.equals(expectedOutputs)) {
                    return true;
                }
            }
        }

        if (cacheFoundResults) {
            cachedResults = results;
        }

        return false;
    }
}
