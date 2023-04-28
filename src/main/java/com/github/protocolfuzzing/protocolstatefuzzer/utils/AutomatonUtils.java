package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Word;

import java.io.Serial;
import java.util.*;

/**
 * Collection of automata related methods and classes.
 */
public class AutomatonUtils {

    /**
     * Provides all the reachable states from the initial state of the
     * automaton.
     *
     * @param <S>              the type of states
     * @param <I>              the type of inputs
     *
     * @param automaton        the automaton to be searched
     * @param inputs           the inputs to be used
     * @param reachableStates  the modifiable collection to be used for
     *                         storing the reachable states
     */
    public static <S,I> void reachableStates(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<I> inputs, Collection<S> reachableStates) {

        reachableStates(automaton, inputs, automaton.getInitialState(), reachableStates);
    }

    /**
     * Provides all the reachable states from a given state of the
     * automaton.
     *
     * @param <S>              the type of states
     * @param <I>              the type of inputs
     *
     * @param automaton        the automaton to be searched
     * @param inputs           the inputs to be used
     * @param fromState        the state from which the search will start
     * @param reachableStates  the modifiable collection to be used for
     *                         storing the reachable states
     */
    public static <S,I> void reachableStates(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton,
        Collection<I> inputs, S fromState, Collection<S> reachableStates) {

        Queue<S> toVisit = new ArrayDeque<>();
        Set<S> visited = new HashSet<>();
        Set<S> reachable = new LinkedHashSet<>();

        toVisit.add(fromState);

        while(!toVisit.isEmpty()) {
            S state = toVisit.poll();
            visited.add(state);
            reachable.add(state);

            for (I input : inputs) {
                for (S nextState : automaton.getSuccessors(state, input)) {
                    if (!visited.contains(nextState)) {
                        toVisit.add(nextState);
                    }
                }
            }
        }

        reachableStates.addAll(reachable);
    }

    /**
     * Provides all the words of inputs that lead from the initial state to the
     * target state of the automaton using a predecessor map generated with
     * {@link #generatePredecessorMap}.
     *
     * @param <S>          the type of states
     * @param <I>          the type of inputs
     *
     * @param automaton    the automaton to be used
     * @param inputs       the inputs to be used
     * @param targetState  the target state where the words will lead to
     * @param words        the modifiable collection that will be used to
     *                     store the resulting words
     */
    public static <S,I> void wordsToTargetState(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton,
        Collection<I> inputs, S targetState, Collection<Word<I>> words) {

        PredMap<S,I> predMap = generatePredecessorMap(automaton, inputs);
        wordsToTargetState(automaton, inputs, targetState, predMap, words);
    }

    /**
     * Provides all the words of inputs that lead from the initial state to the
     * target state of the automaton using the provided predecessor map.
     *
     * @param <S>          the type of states
     * @param <I>          the type of inputs
     *
     * @param automaton    the automaton to be used
     * @param inputs       the inputs to be used
     * @param targetState  the target state where the words will lead to
     * @param map          the predecessor map to be used
     * @param words        the modifiable collection that will be used to
     *                     store the resulting words
     */
    public static <S,I> void wordsToTargetState(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton,
        Collection<I> inputs, S targetState, PredMap<S,I> map,
        Collection<Word<I>> words) {

        Queue<VisitStruct<S,I>> toVisit = new ArrayDeque<>();
        Set<S> hs = new HashSet<>();

        hs.add(targetState);
        toVisit.add(new VisitStruct<>(targetState, Word.epsilon(), hs));

        while (!toVisit.isEmpty()) {
            VisitStruct<S, I> visitStruct = toVisit.poll();
            Collection<PredStruct<S,I>> predStructs = map.get(visitStruct.getState());

            if (predStructs == null) {
                continue;
            }

            for (PredStruct<S,I> predStruct : predStructs) {
                if (predStruct.getState().equals(automaton.getInitialState())) {
                    words.add(Word.fromLetter(predStruct.getInput()).concat(visitStruct.getWord()));
                    continue;
                }

                if (!visitStruct.hasVisited(predStruct.getState())) {
                    HashSet<S> stateSet = new HashSet<>(visitStruct.getVisited());
                    stateSet.add(predStruct.getState());
                    toVisit.add(new VisitStruct<>(
                        predStruct.getState(),
                        Word.fromLetter(predStruct.getInput()).concat(visitStruct.getWord()),
                        stateSet));
                }
            }
        }
    }

    /**
     * Generates a {@link AutomatonUtils.PredMap} of the automaton using the
     * given inputs.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     *
     * @param automaton  the automaton to be used
     * @param inputs     the inputs to be used
     * @return           the generated {@link AutomatonUtils.PredMap}
     */
    public static <S,I> PredMap<S,I> generatePredecessorMap(
        UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
        Collection<I> inputs) {

        PredMap<S,I> predMap = new PredMap<>();
        for (S s : automaton.getStates()) {
            for (I input : inputs) {
                S succ = automaton.getSuccessor(s, input);
                if (succ != null) {
                    predMap.putIfAbsent(succ, new LinkedHashSet<>());
                    predMap.get(succ).add(new PredStruct<>(s, input));
                }
            }
        }
        return predMap;
    }

    /**
     * Contains information about a specific state, like the word leading to it
     * and the states that are visited from it.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     */
    protected static class VisitStruct<S,I> {

        /** Stores the constructor parameter. */
        protected S state;

        /** Stores the constructor parameter. */
        protected Word<I> word;

        /** Stores the constructor parameter. */
        protected Set<S> visited;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param state    the specified state
         * @param word     the word leading to this state
         * @param visited  the set of states that have been visited
         */
        public VisitStruct(S state, Word<I> word, Set<S> visited) {
            this.state = state;
            this.word = word;
            this.visited = visited;
        }

        /**
         * Returns the word leading to the state.
         *
         * @return  the word leading to the state
         */
        public Word<I> getWord() {
            return word;
        }

        /**
         * Returns the state provided in the constructor.
         *
         * @return  the state provided in the constructor
         */
        public S getState() {
            return state;
        }

        /**
         * Checks if the given state is contained in the visited set of states.
         *
         * @param state  the state that should be checked
         * @return       {@code true} if the given state is contained in
         *               the visited set of states {@link #visited}
         */
        public boolean hasVisited(S state) {
            return visited.contains(state);
        }

        /**
         * Returns the set of visited states.
         *
         * @return  the set of visited states
         */
        public Set<S> getVisited() {
            return visited;
        }
    }


    /**
     * Maps a state of an automaton to a collection of
     * {@link AutomatonUtils.PredStruct}.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     */
    public static class PredMap <S,I> extends LinkedHashMap<S, Collection<PredStruct<S, I>>>{
        @Serial
        private static final long serialVersionUID = 1L;
    }

    /**
     * Holds information about a predecessor state of a specified state and
     * the input that leads from the predecessor state to the specified state.
     * <p>
     * The specified state and this class are used in
     * {@link AutomatonUtils.PredMap}.
     *
     * @param <S>        the type of states
     * @param <I>        the type of inputs
     */
    public static class PredStruct <S,I> {

        /** Stores the constructor parameter. */
        protected S state;

        /** Stores the constructor parameter. */
        protected I input;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param state  the predecessor state of a specified state
         * @param input  the input from the predecessor state to the specified
         *               state
         */
        public PredStruct(S state, I input) {
            this.state = state;
            this.input = input;
        }

        /**
         * Returns the predecessor state.
         *
         * @return  the predecessor state
         */
        public S getState() {
            return state;
        }

        /**
         * Returns the input from the predecessor state to the specified state.
         *
         * @return  the input from the predecessor state to the specified state
         */
        public I getInput() {
            return input;
        }
    }
}
