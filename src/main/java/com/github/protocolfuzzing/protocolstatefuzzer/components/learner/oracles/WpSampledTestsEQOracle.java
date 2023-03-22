package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * This EQ Oracle operates similarly to Wp-Random with the difference that the
 * middle sequence is derived from a set of logs. Concretely, the middle
 * sequence is obtained by selecting a suffix of arbitrary length from an
 * arbitrarily chosen log.
 */
public class WpSampledTestsEQOracle<I, O> implements EquivalenceOracle.MealyEquivalenceOracle<I, O> {
    MealyMembershipOracle<I, O> sulOracle;
    protected List<Word<I>> tests;
    protected Random rand;
    protected int bound;
    protected int minimalSize;
    protected int rndLength;

    public WpSampledTestsEQOracle(List<Word<I>> tests, MealyMembershipOracle<I, O> sulOracle, int minimalSize,
                                  int rndLength, long seed, int bound) {
        this.tests = tests;
        this.sulOracle = sulOracle;
        this.rand = new Random(seed);
        this.bound = bound;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
    }

    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(
            MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {
        return doFindCounterExample(hypothesis, inputs);
    }

    /*
     * Delegate target, used to bind the state-parameter of the automaton
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
            if (!Objects.equals(hypOutput, query.getOutput())) return query;
        }
        return null;
    }
}
