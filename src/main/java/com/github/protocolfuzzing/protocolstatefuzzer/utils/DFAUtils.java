package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.word.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Collection of DFA automata related methods.
 */
public class DFAUtils extends AutomatonUtils {

    /**
     * Converts a deterministic Mealy Machine to an equivalent DFA.
     * <p>
     * Inputs/outputs are mapped to corresponding labels given the provided
     * input and output mappings. An output can be mapped to zero, one or
     * several labels (which will be chained one after the other in the model).
     *
     * @param <MI>           the type of Mealy Machine inputs
     * @param <MS>           the type of Mealy Machine states
     * @param <MO>           the type of Mealy Machine outputs
     * @param <DI>           the type of DFA inputs
     * @param <DS>           the type of DFA states
     * @param <DA>           the type of DFA automaton
     *
     * @param mealy          the Mealy Machine to be converted
     * @param inputs         the inputs of the Mealy Machine
     * @param labels         the labels used during the completion of the DFA
     * @param inputMapping   the mapping from Mealy Machine inputs to DFA
     *                       alphabet symbols
     * @param outputMapping  the mapping from Pairs of Mealy Machine state
     *                       and output to a list of DFA alphabet symbols
     * @param stateMapping   the modifiable mapping from Mealy Machine states
     *                       to DFA states, which is populated after the conversion
     * @param dfa            the modifiable DFA which is altered and also returned
     *
     * @return   The provided parameter dfa after modification that led to an
     *           alphabet-complete DFA non-minimized so as to resemble the original
     *           model. Minimization can be achieved via minimize methods in
     *           net.automatalib.util.automata.Automata
     */
    public static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>> DA convertMealyToDFA(
        MealyMachine<MS, MI, ?, MO> mealy, Collection<MI> inputs, Collection<DI> labels,
        Mapping<MI,DI> inputMapping, Mapping<Pair<MS,MO>, List<DI>> outputMapping,
        Map<MS,DS> stateMapping, DA dfa) {

        MS mealyState = mealy.getInitialState();
        Map<MS, DS> inputStateMapping = new HashMap<>();
        DS dfaState = dfa.addInitialState(true);
        inputStateMapping.put(mealyState, dfaState);
        Set<MS> visited = new HashSet<>();
        convertMealyToDFA(mealyState, dfaState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
        MutableDFAs.complete(dfa, labels, false, false);
        stateMapping.putAll(inputStateMapping);
        return dfa;
    }

    /**
     * Converts a deterministic Mealy Machine to an equivalent DFA.
     * <p>
     * Inputs/outputs are mapped to corresponding labels given the provided
     * input and output mappings. An output can be mapped to zero, one or
     * several labels (which will be chained one after the other in the model).
     *
     * @param <MI>               the type of Mealy Machine inputs
     * @param <MS>               the type of Mealy Machine states
     * @param <MO>               the type of Mealy Machine outputs
     * @param <DI>               the type of DFA inputs
     * @param <DS>               the type of DFA states
     * @param <DA>               the type of DFA automaton
     *
     * @param mealyState         the initial Mealy Machine state
     * @param dfaState           the initial DFA state
     * @param mealy              the Mealy Machine to be converted
     * @param inputs             the inputs of the Mealy Machine
     * @param inputMapping       the mapping from Mealy Machine inputs to DFA
     *                           alphabet symbols
     * @param outputMapping      the mapping from Pairs of Mealy Machine state
     *                           and output to a list of DFA alphabet symbols
     * @param inputStateMapping  the modifiable mapping from Mealy Machine states
     *                           to DFA states, which is populated after the conversion
     * @param visited            a modifiable set for the visited Mealy Machine states
     * @param dfa                the modifiable DFA which is altered
     */
    protected static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>> void convertMealyToDFA(
        MS mealyState, DS dfaState, MealyMachine<MS, MI, ?, MO> mealy,
        Collection<MI> inputs, Mapping<MI, DI> inputMapping,
        Mapping<Pair<MS,MO>, List<DI>> outputMapping,
        Map<MS, DS> inputStateMapping, Set<MS> visited, DA dfa) {

        inputStateMapping.put(mealyState, dfaState);
        DS inputState = dfaState;
        visited.add(mealyState);
        DS nextInputState;
        for (MI input : inputs) {
            DI inputLabel = inputMapping.get(input);
            MO output = mealy.getOutput(mealyState, input);
            MS nextMealyState = mealy.getSuccessor(mealyState, input);

            nextInputState = inputStateMapping.get(nextMealyState);
            if (nextInputState == null) {
                nextInputState = dfa.addState(true);
                inputStateMapping.put(nextMealyState, nextInputState);
            }

            Collection<DI> outputLabels = outputMapping.get(Pair.of(mealyState, output));
            List<DI> labels = new ArrayList<>(outputLabels.size()+1);
            labels.add(inputLabel);
            labels.addAll(outputLabels);

            DS nextState;
            DS lastState = inputState;
            for (int i = 0; i < labels.size() - 1; i++) {
                DI ioLabel = labels.get(i);
                nextState = dfa.addState(true);
                dfa.addTransition(lastState, ioLabel, nextState);
                lastState = nextState;
            }

            dfa.addTransition(lastState, labels.get(labels.size() - 1), nextInputState);

            if (!visited.contains(nextMealyState)) {
                convertMealyToDFA(nextMealyState, nextInputState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
            }
        }
    }

    /**
     * Generates a rejecting DFA that consists of a single non-accepting state
     * with all inputs causing self-loop transitions from/to this single state.
     *
     * @param <I>       the type of inputs
     *
     * @param alphabet  the alphabet of the DFA
     * @return          the rejecting DFA
     */
    public static <I> DFA<?,I> buildRejecting(Collection<I> alphabet) {
        FastDFA<I> rejectingModel = new FastDFA<>(new ListAlphabet<>(new ArrayList<>(alphabet)));
        FastDFAState rej = rejectingModel.addInitialState(false);
        for (I label : alphabet) {
            rejectingModel.addTransition(rej, label, rej);
        }
        return rejectingModel;
    }

    /**
     * Determines if there is any path that leads from a given state of the DFA
     * after any number of inputs to an accepting state of the DFA.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     *
     * @param state      the state from which the search will start
     * @param automaton  the DFA automaton
     * @param alphabet   the alphabet of the DFA
     * @return           {@code true} if there is such a path
     */
    public static <S,I> boolean hasAcceptingPaths(S state, DFA<S, I> automaton, Collection<I> alphabet) {
        Set<S> reachableStates = new HashSet<>();
        reachableStates(automaton, alphabet, state, reachableStates);
        return reachableStates.stream().anyMatch(automaton::isAccepting);
    }

    /**
     * Finds the shortest accepting word in a DFA.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     *
     * @param automaton  the DFA automaton
     * @param alphabet   the alphabet of the DFA
     * @return           the word of alphabet symbols or null if there is no such word
     */
    public static <S,I> Word<I> findShortestAcceptingWord(DFA<S, I> automaton, Collection<I> alphabet) {

        return DeterministicEquivalenceTest.findSeparatingWord(DFAs.complete(automaton, new ListAlphabet<>(new ArrayList<>(alphabet))), buildRejecting(alphabet), alphabet);

        /*
        ModelExplorer<S, I> explorer = new ModelExplorer<S, I>(automaton, inputs);
        List<S> acceptingStates = automaton.getStates().stream()
                .filter(s -> automaton.isAccepting(s))
                .collect(Collectors.toList());
        if (!acceptingStates.isEmpty()) {
            SearchConfig config = new SearchConfig();
            config.setStateVisitBound(1);
            Iterable<Word<I>> words = explorer.wordsToTargetStates(acceptingStates, config);
            Iterator<Word<I>> iter = words.iterator();
            if (iter.hasNext()) {
                return iter.next();
            }
        }
        return null;
        */
    }

    /**
     * Finds the shortest non-accepting prefix of a word of alphabet symbols
     * in a DFA.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     *
     * @param automaton  the DFA automaton
     * @param word       the word of alphabet symbols of the DFA
     * @return           the desired prefix of the word or null if there is no
     *                   such prefix
     */
    public static <S,I> Word<I> findShortestNonAcceptingPrefix(DFA<S, I> automaton, Word<I> word) {
        int prefixLen = 0;
        S currState = automaton.getInitialState();

        for (I input : word) {
            if (currState == null || !automaton.isAccepting(currState)) {
                return word.prefix(prefixLen);
            }
            prefixLen++;
            currState = automaton.getSuccessor(currState, input);
        }

        if (currState == null || !automaton.isAccepting(currState)) {
            return word.prefix(prefixLen);
        }

        return null;
    }
}
