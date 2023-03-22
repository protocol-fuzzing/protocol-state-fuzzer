package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automata.transducers.MealyMachine;

import java.util.*;

public class MealyUtils extends AutomatonUtils{


	/**
	 * Determines all the outputs the model can generate in response to the given inputs.
	 * @param automaton
	 * @param inputs
	 * @param reachableOutputs
	 */
	public static <S,I,O> void reachableOutputs(MealyMachine<S, I, ?, O> automaton, Collection<I> inputs, Collection<O> reachableOutputs) {
		Queue<S> reachableStates = new ArrayDeque<>();
		Set<O> outputs = new HashSet<>();
		reachableStates(automaton, inputs, reachableStates);
		for (S state : reachableStates) {
			for (I input : inputs) {
				outputs.add(automaton.getOutput(state, input));
			}
		}
		reachableOutputs.addAll(outputs);
	}

}
