package io.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Equivalence Oracle for the
 * {@link io.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName#SAMPLED_TESTS_RA}.
 */
public class SampledTestsEQOracleRA implements IOEquivalenceOracle {

    /** Stores tests. */
    protected List<Word<PSymbolInstance>> tests;

    /** Stores the constructor parameter. */
    protected SULOracle sulOracle;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param tests the list of tests to be sampled
     * @param sul   the SUL to be used
     */
    public SampledTestsEQOracleRA(List<Word<PSymbolInstance>> tests, DataWordSUL sul) {
        this.tests = tests;
        this.sulOracle = new SULOracle(sul, new OutputSymbol("_io_err"));
    }

    /**
     * Tries to find a counterexample using the sampled tests technique.
     *
     * @param  hypothesis the hypothesis to be searched
     * @param  inputs     the inputs to be used
     *
     * @return            the counterexample or null
     */

    @Override
    public @Nullable DefaultQuery<PSymbolInstance, Boolean> findCounterExample(RegisterAutomaton hypothesis,
        Collection<? extends PSymbolInstance> inputs) {
        for (Word<PSymbolInstance> test: tests) {
            // trace captures a valid execution of the SUL, which should be accepted by the hypothesis
            Word<PSymbolInstance> trace = sulOracle.trace(test);
            if (!hypothesis.accepts(trace)) {
                // we have a CE, now we return the most minimal one
                for (int i = 2; i <= trace.length(); i++) {
                    if (!hypothesis.accepts(trace.prefix(i))) {
                        return new DefaultQuery<>(trace.prefix(i), true);
                    }
                }
            }
        }
        return null;
    }
}
