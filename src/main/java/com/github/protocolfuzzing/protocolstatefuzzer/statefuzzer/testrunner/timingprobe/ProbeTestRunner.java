package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import net.automatalib.words.Word;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TestRunner extended to be used by the TimingProbe.
 */
public class ProbeTestRunner extends TestRunner {

    /** Stores a list of results. */
    protected List<TestRunnerResult<AbstractInput, AbstractOutput>> cachedResults = null;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @param alphabetBuilder    the builder of the alphabet
     * @param sulBuilder         the builder of the sul
     * @param sulWrapper         the wrapper of the sul
     */
    public ProbeTestRunner(TestRunnerEnabler testRunnerEnabler, AlphabetBuilder alphabetBuilder,
                            SulBuilder sulBuilder, SulWrapper sulWrapper) {
        super(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    /**
     * Runs the current tests using {@link #runTests()} and checks if they are not deterministic.
     * <p>
     * If there are {@link #cachedResults}, then any new result should correspond
     * to a cached one, with which should have the same outputs.
     *
     * @param cacheFoundResults  cache the found results to {@link #cachedResults}
     * @return                   <code>true</code> if any result is found to be
     *                           non-deterministic
     *
     * @throws IOException       if an error occurs during {@link #runTests()}
     */
    public boolean isNonDeterministic(boolean cacheFoundResults) throws IOException {
        List<TestRunnerResult<AbstractInput, AbstractOutput>> results = super.runTests();
        Iterator<TestRunnerResult<AbstractInput, AbstractOutput>> iterator = null;

        if (cachedResults != null) {
            iterator = cachedResults.iterator();
        }

        for (TestRunnerResult<AbstractInput, AbstractOutput> result : results) {
            Map<Word<AbstractOutput>, Integer> resultOutputs = result.getGeneratedOutputs();

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

                Map<Word<AbstractOutput>, Integer> expectedOutputs = iterator.next().getGeneratedOutputs();

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
