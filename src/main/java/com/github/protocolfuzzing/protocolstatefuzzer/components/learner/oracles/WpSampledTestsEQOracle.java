package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


/**
 * Equivalence Oracle for the
 * {@link com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName#WP_SAMPLED_TESTS}.
 * <p>
 * This Oracle operates similarly to WP-Random with the difference that the
 * middle sequence is derived from a set of logs. Specifically, the middle
 * sequence is obtained by selecting a suffix of arbitrary length from an
 * arbitrarily chosen log.
 *
 * @param <I>  the type of the inputs
 * @param <O>  the type of the outputs
 */
public class WpSampledTestsEQOracle<I, O> implements EquivalenceOracle.MealyEquivalenceOracle<I, O> {

    /** Stores the constructor parameter. */
    protected List<Word<I>> tests;

    /** Stores the constructor parameter. */
    MealyMembershipOracle<I, O> sulOracle;

    /** Stores the constructor parameter. */
    protected int minimalSize;

    /** Stores the constructor parameter. */
    protected int rndLength;

    /** Stores the constructor parameter seed wrapped in {@link Random}. */
    protected Random rand;

    /** Stores the constructor parameter. */
    protected int bound;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param tests        the list of tests to be sampled
     * @param sulOracle    the sul oracle to be used
     * @param minimalSize  the minimal size of middle sequence
     * @param rndLength    the random length of middle sequence
     * @param seed         the seed used for randomness
     * @param bound        the upper bound of sampling iterations
     */
    public WpSampledTestsEQOracle(List<Word<I>> tests,
        MealyMembershipOracle<I, O> sulOracle, int minimalSize,
        int rndLength, long seed, int bound) {

        this.tests = tests;
        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.rand = new Random(seed);
        this.bound = bound;
    }

    /**
     * Tries to find a counterexample using {@link #doFindCounterExample(MealyMachine, Collection)}.
     *
     * @param hypothesis  the hypothesis to be searched
     * @param inputs      the inputs to be used
     * @return            the counterexample or null
     */
    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(
            MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {

        return doFindCounterExample(hypothesis, inputs);
    }

    /**
     * Implements the search technique.
     *
     * @param <S>         the type of a state
     * @param hypothesis  the hypothesis to be searched
     * @param inputs      the inputs to be used
     * @return            the counterexample or null
     */
    protected <S> DefaultQuery<I, Word<O>> doFindCounterExample(
            MealyMachine<S, I, ?, O> hypothesis, Collection<? extends I> inputs) {

        WpEQSequenceGenerator<I, Word<O>, S> generator = new WpEQSequenceGenerator<>(hypothesis, inputs);
        List<S> states = new ArrayList<>(hypothesis.getStates());

        for (int i = 0; i < bound; i++) {
            S randState = states.get(rand.nextInt(states.size()));
            Word<I> randAccSeq = generator.getRandomAccessSequence(randState, rand);
            Word<I> middlePart;

            if (rand.nextBoolean() && !tests.isEmpty()) {
                Word<I> randTest = tests.get(rand.nextInt(tests.size()));
                middlePart = randTest.suffix(rand.nextInt(randTest.length()));
            } else {
                middlePart = generator.getRandomMiddleSequence(minimalSize, rndLength, rand);
            }

            Word<I> distSequence = generator.getRandomCharacterizingSequence(randAccSeq.concat(middlePart), rand);
            Word<I> test = randAccSeq.concat(middlePart, distSequence);
            Word<O> hypOutput = hypothesis.computeOutput(test);
            DefaultQuery<I, Word<O>> query = new DefaultQuery<>(test);

            sulOracle.processQueries(Collections.singleton(query));

            if (!Objects.equals(hypOutput, query.getOutput())) {
                return query;
            }
        }

        return null;
    }
}
