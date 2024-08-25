package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


/**
 * Implements an equivalence test by applying the WP-method test on the given
 * hypothesis automaton as described in "Test Selection Based on Finite State Models" by {@literal S. Fujiwara et al}.
 * <p>
 * Adapted from an EQ oracle implementation in LearnLib's development branch not
 * available in the version we use, see
 * <a href="https://github.com/mtf90/learnlib/blob/develop/oracles/equivalence-oracles/src/main/java/de/learnlib/oracle/equivalence/RandomWpMethodEQOracle.java">RandomWpMethodEQOracle</a>.
 * Our adaptation is randomizing access sequence generation.
 * <p>
 * Instead of enumerating the test suite in order, this is a sampling implementation:
 * <ol>
 * <li> Sample uniformly from the states for a prefix
 * <li> Sample geometrically a random word
 * <li> Sample a word from the set of suffixes / state identifiers (either local or global).
 * </ol>
 * <p>
 * There are two parameters:
 * <ul>
 * <li> minimalSize determines the minimal size of the random word. This is useful when one first performs a
 *      W(p)-method with some depth and continues with this randomized tester from that depth onward
 * <li> rndLength determines the expected length of the random word. The expected length in effect is minimalSize + rndLength.
 *      In the unbounded case it will not terminate for a correct hypothesis.
 * </ul>
 *
 * @param <I>  input symbol type
 * @param <O>  output symbol type
 */
public class RandomWpMethodEQOracle<I,O> implements EquivalenceOracle.MealyEquivalenceOracle<I, O> {

    /** Stores the constructor parameter. */
    protected MealyMembershipOracle<I, O>  sulOracle;

    /** Stores the constructor parameter. */
    protected int minimalSize;

    /** Stores the constructor parameter. */
    protected int rndLength;

    /** Stores the constructor parameter. */
    protected int bound;

    /** Stores the constructor parameter. */
    protected long seed;

    /**
     * Constructs a new instance from the given parameters, which represents an unbounded testing oracle.
     *
     * @param sulOracle    the oracle which answers tests
     * @param minimalSize  the minimal size of the random word
     * @param rndLength    the expected length (in addition to minimalSize) of random word
     * @param seed         the seed to be used for randomness
     */
    public RandomWpMethodEQOracle(MealyMembershipOracle<I, O> sulOracle,
        int minimalSize, int rndLength, long seed) {

        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.seed = seed;
        this.bound = 0;
    }

    /**
     * Constructs a new instance from the given parameters, which represents a bounded testing oracle.
     *
     * @param sulOracle    the oracle which answers tests
     * @param minimalSize  the minimal size of the random word
     * @param rndLength    the expected length (in addition to minimalSize) of random word
     * @param bound        the bound (set to 0 for unbounded).
     * @param seed         the seed to be used for randomness
     */
    public RandomWpMethodEQOracle(MealyMembershipOracle<I, O> sulOracle,
        int minimalSize, int rndLength, int bound, long seed) {

        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = bound;
        this.seed = seed;
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
    public <S> @Nullable DefaultQuery<I, Word<O>> doFindCounterExample(MealyMachine<S, I, ?, O> hypothesis,
        Collection<? extends I> inputs) {
        WpEQSequenceGenerator<I, Word<O>, S> generator = new WpEQSequenceGenerator<>(hypothesis, inputs);

        Random rand = new Random(seed);
        List<S> states = new ArrayList<>(hypothesis.getStates());
        int currentBound = bound;

        while (bound == 0 || currentBound-- > 0) {
            WordBuilder<I> wb = new WordBuilder<>(minimalSize + rndLength + 1);

            // pick a random state
            wb.append(generator.getRandomAccessSequence(
                states.get(rand.nextInt(states.size())), rand));

            // construct random middle part (of some expected length)
            wb.append(generator.getRandomMiddleSequence(minimalSize, rndLength, rand));

            // construct a random characterizing/identifying sequence
            wb.append(generator.getRandomCharacterizingSequence(wb, rand));

            Word<I> queryWord = wb.toWord();
            Word<O> hypOutput = hypothesis.computeOutput(queryWord);
            DefaultQuery<I, Word<O>> query = new DefaultQuery<>(queryWord);

            sulOracle.processQueries(Collections.singleton(query));

            if (!Objects.equals(hypOutput, query.getOutput())) {
                return query;
            }
        }

        // no counter example found within the bound
        return null;
    }
}
