package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.AutomatonUtils.PredStruct;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.*;

/**
 * Sequence generation method that randomizes access sequences.
 * <p>
 * Factored out from
 * <a href="https://github.com/mtf90/learnlib/blob/develop/eqtests/basic-eqtests/src/main/java/de/learnlib/eqtests/basic/RandomWpMethodEQOracle.java">RandomWpMethodEQOracle</a>.
 * <p>
 * The key difference is that we randomize access sequences.
 *
 * @param <I>  the type of inputs
 * @param <D>  the type of output domain
 * @param <S>  the type of states
 */
public class WpEQSequenceGenerator<I, D, S> {

    /** Stores the constructor parameter. */
    protected UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton;

    /** Stores the constructor parameter. */
    protected Collection<? extends I> inputs;

    /** The list of global suffixes. */
    protected ArrayList<Word<I>> globalSuffixes;

    /** The set of local suffixes. */
    protected MutableMapping<S, ArrayList<Word<I>>> localSuffixSets;

    /** The map holding the predecessors of {@link #inputs}. */
    protected Map<S, List<PredStruct<S, I>>> predMap;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     */
    public WpEQSequenceGenerator(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs) {

        this.automaton = automaton;
        this.inputs = inputs;
        globalSuffixes = computeGlobalSuffixes(automaton, inputs);
        localSuffixSets = computeLocalSuffixSets(automaton, inputs);
        predMap = computePredecessorMap(automaton, inputs);
    }

    /**
     * Computes the predecessor map of automaton inputs.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     * @return           the predecessor map
     */
    public static <S, I> Map<S, List<PredStruct<S, I>>> computePredecessorMap(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs) {

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

    /**
     * Computes the global suffixes of the automaton.
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     * @return           the list of global suffixes
     */
    protected ArrayList<Word<I>> computeGlobalSuffixes(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs) {

        ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
        Automata.characterizingSet(automaton, inputs, globalSuffixes);
        return globalSuffixes;
    }

    /**
     * Computes the local suffixes of the automaton.
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     * @return           the list of local suffixes
     */
    protected MutableMapping<S, ArrayList<Word<I>>> computeLocalSuffixSets(
        UniversalDeterministicAutomaton<S, I, ?, ?,?> automaton,
        Collection<? extends I> inputs) {

        MutableMapping<S, ArrayList<Word<I>>> localSuffixSets = automaton.createStaticStateMapping();
        for (S state : automaton.getStates()) {
            ArrayList<Word<I>> suffixSet = new ArrayList<>();
            Automata.stateCharacterizingSet(automaton, inputs, state, suffixSet);
            localSuffixSets.put(state, suffixSet);
        }
        return localSuffixSets;
    }

    /**
     * Constructs the random middle sequence of an expected length.
     *
     * @param minimalSize  the minimal size of the sequence
     * @param rndLength    length used for the random length generation
     * @param rand         a Random instance used for the random length generation
     * @return             the constructed middle sequence
     */
    public Word<I> getRandomMiddleSequence(int minimalSize, int rndLength, Random rand) {
        ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);
        WordBuilder<I> wb = new WordBuilder<>();
        int size = minimalSize;

        while ((size > 0) || (rand.nextDouble() > 1 / (rndLength + 1.0))) {
            wb.append(arrayAlphabet.get(rand.nextInt(arrayAlphabet.size())));
            if (size > 0) size--;
        }

        return wb.toWord();
    }

    /**
     * Returns a random characterizing sequence of the given sequence using
     * {@link #getRandomCharacterizingSequence(UniversalDeterministicAutomaton, Collection, Iterable, Random)}.
     * <p>
     * The {@link #automaton} and {@link #inputs} are also used.
     *
     * @param fromSequence  the initial sequence to be used
     * @param rand          a Random instance to be used
     * @return              the random characterizing sequence
     */
    public Word<I> getRandomCharacterizingSequence(Iterable<I> fromSequence, Random rand) {
        return getRandomCharacterizingSequence(automaton, inputs, fromSequence, rand);
    }

    /**
     * Returns a random characterizing sequence of the given sequence.
     *
     * @param automaton     the automaton to be used
     * @param inputs        the inputs of the automaton
     * @param fromSequence  the initial sequence to be used
     * @param rand          a Random instance to be used
     * @return              the random characterizing sequence
     */
    protected Word<I> getRandomCharacterizingSequence(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs,
        Iterable<I> fromSequence,
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

    /**
     * Returns a random access sequence of the given state using
     * {@link #getRandomAccessSequence(UniversalDeterministicAutomaton, Collection, Object, Random)}.
     * <p>
     * The {@link #automaton} and {@link #inputs} are also used.
     *
     * @param toState  the target state to be used
     * @param rand     a Random instance to be used
     * @return         the random access sequence
     *
     * @throws IllegalStateException  if the access sequence cannot be generated
     */
    public Word<I> getRandomAccessSequence(S toState, Random rand) {
        return getRandomAccessSequence(automaton, inputs, toState, rand);
    }

    /**
     * Returns a random access sequence of the given state using
     * {@link #getRandomAccessSequence(UniversalDeterministicAutomaton, Collection, Object, Random, Object, Set, List)}.
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     * @param toState    the target state to be used
     * @param rand       a Random instance to be used
     * @return           the random access sequence
     *
     * @throws IllegalStateException  if the access sequence cannot be generated
     */
    protected Word<I> getRandomAccessSequence(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs,
        S toState,
        Random rand) {

        Set<S> hs = new HashSet<>();
        hs.add(toState);

        Word<I> accessSequence = getRandomAccessSequence(automaton, inputs, toState, rand, toState, hs, new ArrayList<>());

        if (accessSequence == null) {
            throw new IllegalStateException("Access sequence could not be generated");
        }

        return accessSequence;
    }

    /**
     * Returns a random access sequence of the given state.
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs of the automaton
     * @param toState    the target state to be used
     * @param rand       a Random instance to be used
     * @param visiting   the state that is being visited
     * @param visited    the set of visited states
     * @param sequence   an external list of input sequence
     * @return           the random access sequence or null
     */
    protected Word<I> getRandomAccessSequence(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<? extends I> inputs,
        S toState,
        Random rand,
        S visiting,
        Set<S> visited,
        List<I> sequence) {

        if (visiting.equals(automaton.getInitialState())) {
            return Word.fromList(sequence);
        }

        List<PredStruct<S, I>> predStructs = predMap.get(visiting);

        if (predStructs != null) {
            predStructs = new ArrayList<>(predStructs);
            Collections.shuffle(predStructs, rand);

            for (PredStruct<S, I> predStruct : predStructs) {
                if (!visited.contains(predStruct.getState())) {
                    visited.add(predStruct.getState());
                    sequence.add(0, predStruct.getInput());

                    Word<I> result = getRandomAccessSequence(
                        automaton, inputs, toState, rand, predStruct.getState(), visited, sequence
                    );

                    if (result != null) {
                        return result;
                    }

                    visited.remove(predStruct.getState());
                    sequence.remove(0);
                }
            }
        }

        return null;
    }
}
