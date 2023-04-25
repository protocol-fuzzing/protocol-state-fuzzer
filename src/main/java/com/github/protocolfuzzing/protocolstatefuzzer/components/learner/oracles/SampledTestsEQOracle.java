package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Equivalence Oracle for the
 * {@link com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName#SAMPLED_TESTS}.
 *
 * @param <I>  the type of the inputs
 * @param <O>  the type of the outputs
 */
public class SampledTestsEQOracle<I,O> implements EquivalenceOracle.MealyEquivalenceOracle<I, O> {

    /** Stores the constructor parameter. */
    protected List<Word<I>> tests;

    /** Stores the constructor parameter. */
    protected MealyMembershipOracle<I, O> sulOracle;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param tests      the list of tests to be sampled
     * @param sulOracle  the sul oracle to be used
     */
    public SampledTestsEQOracle(List<Word<I>> tests, MealyMembershipOracle<I, O> sulOracle) {
        this.tests = tests;
        this.sulOracle = sulOracle;
    }

    /**
     * Tries to find a counterexample using the sampled tests technique.
     *
     * @param hypothesis  the hypothesis to be searched
     * @param inputs      the inputs to be used
     * @return            the counterexample or null
     */
    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(
        MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {

        for (Word<I> test : tests) {
            DefaultQuery<I, Word<O>> query = new DefaultQuery<>(test);
            Word<O> hypOutput = hypothesis.computeOutput(test);

            sulOracle.processQueries(Collections.singleton(query));

            if (!Objects.equals(hypOutput, query.getOutput())) {
                return query;
            }
        }

        return null;
    }
}
