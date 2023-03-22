package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.*;

/**
 * Sequence generation method factored out from the RandomWpMethodEQOracle.
 * <p>
 * The implementation was mostly taken/adapted from Joshua's implementation of
 * the RandomWpMethod
 *
 * <pre>
 * See: <a href="https://github.com/mtf90/learnlib/blob/develop/eqtests/basic-eqtests/src/main/java/de/learnlib/eqtests/basic/RandomWpMethodEQOracle.java">RandomWpMethodEQOracle</a>
 * </pre>
 * <p>
 * Key difference is that we randomize access sequences.
 */
public class WpEQSequenceGenerator<I, D, S> {

    protected UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton;
    protected Collection<? extends I> inputs;
    protected ArrayList<Word<I>> globalSuffixes;
    protected MutableMapping<S, ArrayList<Word<I>>> localSuffixSets;
    protected Map<S, List<PredStruct<S, I>>> predMap;

    public WpEQSequenceGenerator(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<? extends I> inputs) {

        this.automaton = automaton;
        this.inputs = inputs;
        globalSuffixes = computeGlobalSuffixes(automaton, inputs);
        localSuffixSets = computeLocalSuffixSets(automaton, inputs);
        predMap = computePredecessorMap(automaton, inputs);
    }

    public static <S, I, O> Map<S, List<PredStruct<S, I>>> computePredecessorMap(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<? extends I> inputs) {

        Map<S, List<PredStruct<S, I>>> predMap = new HashMap<>();
        for (S s : automaton.getStates()) {
            for (I input : inputs) {
                S succ = automaton.getSuccessor(s, input);
                if (succ != null) {
                    predMap.putIfAbsent(succ, new ArrayList<>());
                    predMap.get(succ).add(new PredStruct<>(s, input));
                }
            }
        }
        return predMap;
    }

    protected ArrayList<Word<I>> computeGlobalSuffixes(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<? extends I> inputs) {

        ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
        Automata.characterizingSet(automaton, inputs, globalSuffixes);
        return globalSuffixes;
    }

    protected MutableMapping<S, ArrayList<Word<I>>> computeLocalSuffixSets(
            UniversalDeterministicAutomaton<S, I, ?, ?,?> automaton, Collection<? extends I> inputs) {

        MutableMapping<S, ArrayList<Word<I>>> localSuffixSets = automaton.createStaticStateMapping();
        for (S state : automaton.getStates()) {
            ArrayList<Word<I>> suffixSet = new ArrayList<>();
            Automata.stateCharacterizingSet(automaton, inputs, state, suffixSet);
            localSuffixSets.put(state, suffixSet);
        }
        return localSuffixSets;
    }

    Word<I> getRandomMiddleSequence(int minimalSize, int rndLength, Random rand) {
        ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);
        WordBuilder<I> wb = new WordBuilder<>();
        // construct random middle part (of some expected length)
        int size = minimalSize;
        while ((size > 0) || (rand.nextDouble() > 1 / (rndLength + 1.0))) {
            wb.append(arrayAlphabet.get(rand.nextInt(arrayAlphabet.size())));
            if (size > 0) size--;
        }
        return wb.toWord();
    }

    Word<I> getRandomCharacterizingSequence(Iterable<I> fromSequence, Random rand) {
        return getRandomCharacterizingSequence(automaton, inputs, fromSequence, rand);
    }

    protected Word<I> getRandomCharacterizingSequence(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
            Collection<? extends I> inputs, Iterable<I> fromSequence,
            Random rand) {

        WordBuilder<I> wb = new WordBuilder<>();

        // pick a random suffix for this state
        // 50% chance for state testing, 50% chance for transition testing
        if (rand.nextBoolean()) {
            // global
            if (!globalSuffixes.isEmpty()) {
                wb.append(globalSuffixes.get(rand.nextInt(globalSuffixes.size())));
            }
        } else {
            // local
            S state2 = automaton.getState(fromSequence);
            ArrayList<Word<I>> localSuffixes = localSuffixSets.get(state2);
            if (!localSuffixes.isEmpty()) {
                wb.append(localSuffixes.get(rand.nextInt(localSuffixes.size())));
            }
        }
        return wb.toWord();
    }

    Word<I> getRandomAccessSequence(S toState, Random rand) {
        return getRandomAccessSequence(automaton, inputs, toState, rand);
    }

    protected Word<I> getRandomAccessSequence(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
            Collection<? extends I> inputs, S toState, Random rand) {

        Set<S> hs = new HashSet<>();
        hs.add(toState);
        Word<I> accessSequence = getRandomAccessSequence(automaton, inputs, toState, rand, toState, hs, new LinkedList<>());

        if (accessSequence == null) {
            throw new IllegalStateException("Access sequence could not be generated");
        } else {
            return accessSequence;
        }
    }

    protected Word<I> getRandomAccessSequence(
            UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<? extends I> inputs,
            S toState, Random rand, S visiting, Set<S> visited, LinkedList<I> sequence) {

        if (visiting.equals(automaton.getInitialState())) {
            return Word.fromList(sequence);
        } else {
            List<PredStruct<S, I>> predStructs = predMap.get(visiting);
            if (predStructs != null) {
                predStructs = new ArrayList<>(predStructs);
                Collections.shuffle(predStructs, rand);

                for (PredStruct<S, I> predStruct : predStructs) {
                    if (!visited.contains(predStruct.getState())) {
                        visited.add(predStruct.getState());
                        sequence.addFirst(predStruct.getInput());
                        Word<I> result = getRandomAccessSequence(automaton, inputs, toState, rand,
                                predStruct.getState(), visited, sequence);
                        if (result != null) {
                            return result;
                        } else {
                            visited.remove(predStruct.getState());
                            sequence.removeFirst();
                        }
                    }
                }
            }
            return null;
        }
    }

    protected static class PredStruct<S, I> {
        private S state;
        private I input;

        public PredStruct(S state, I input) {
            super();
            this.state = state;
            this.input = input;
        }

        public S getState() {
            return state;
        }

        public I getInput() {
            return input;
        }
    }
}
