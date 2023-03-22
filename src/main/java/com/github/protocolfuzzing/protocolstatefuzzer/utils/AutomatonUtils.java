package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Word;

import java.io.Serial;
import java.util.*;

public class AutomatonUtils {

	/**
	 * Determines all the states which can be reached using the given inputs on the automaton.
	 * @param automaton
	 * @param inputs
	 * @param reachableStates
	 */
	public static <S,I> void reachableStates(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<I> inputs, Collection<S> reachableStates) {
		reachableStates(automaton, inputs, automaton.getInitialState(), reachableStates);
	}

	public static <S,I> void reachableStates(UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton, Collection<I> inputs, S fromState, Collection<S> reachableStates) {
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

	public static <S,I> void wordsToTargetState(UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton,
			Collection<I> inputs, S targetState, Collection<Word<I>> words) {
		PredMap<S,I> predMap = generatePredecessorMap(automaton, inputs);
		wordsToTargetState(automaton, inputs, targetState, predMap, words);
	}

	public static <S,I> void wordsToTargetState(UniversalDeterministicAutomaton<S, I, ?, ?, ?>  automaton,
			Collection<I> inputs, S targetState,
			PredMap<S,I> map, Collection<Word<I>> words) {
		Queue<VisitStruct<S,I>> toVisit = new ArrayDeque<>();
		Set<S> hs = new HashSet<>();
		hs.add(targetState);
		toVisit.add(new VisitStruct<>(targetState, Word.epsilon(), hs));
		while (!toVisit.isEmpty()) {
			VisitStruct<S, I> visitStruct = toVisit.poll();
			Collection<PredStruct<S,I>> predStructs = map.get(visitStruct.getState());
			if (predStructs != null) {
				for (PredStruct<S,I> predStruct : predStructs) {
					if (predStruct.getState().equals(automaton.getInitialState())) {
						words.add(Word.fromLetter(predStruct.getInput()).concat(visitStruct.getWord()));
					} else {
						if (!visitStruct.hasVisited(predStruct.getState())) {
							HashSet<S> stateSet = new HashSet<>(visitStruct.getVisited());
							stateSet.add(predStruct.getState());
							toVisit.add(new VisitStruct<>(predStruct.getState(), Word.fromLetter(predStruct.getInput()).concat(visitStruct.getWord()), stateSet));
						}
					}
				}
			}
		}
	}

	public static <S,I> PredMap<S,I> generatePredecessorMap(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton, Collection<I> inputs) {
		PredMap<S,I> predMap = new PredMap<>();
		for (S s : automaton.getStates()) {
			for (I input : inputs) {
				S succ= automaton.getSuccessor(s, input);
				if (succ != null) {
					predMap.putIfAbsent(succ, new LinkedHashSet<>());
					predMap.get(succ).add(new PredStruct<>(s, input));
				}
			}
		}
		return predMap;
	}

	protected static class VisitStruct<S,I> {
		private Word<I> word;
		private Set<S> visited;
		private S state;
		public Word<I> getWord() {
			return word;
		}
		public S getState() {
			return state;
		}

		public boolean hasVisited(S state) {
			return visited.contains(state);
		}

		public Set<S> getVisited() {
			return visited;
		}

		public VisitStruct(S state, Word<I> word, Set<S> visited) {
			super();
			this.word = word;
			this.state = state;
			this.visited = visited;
		}
	}


	public static class PredMap <S,I> extends LinkedHashMap<S, Collection<PredStruct<S, I>>>{
		@Serial
		private static final long serialVersionUID = 1L;

	}

	public static class PredStruct <S,I> {
		private S state;
		private I input;
		public S getState() {
			return state;
		}
		public PredStruct(S state, I input) {
			super();
			this.state = state;
			this.input = input;
		}

		public I getInput() {
			return input;
		}

	}
}
