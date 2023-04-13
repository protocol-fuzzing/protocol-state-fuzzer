package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automata.transducers.MealyMachine;

import java.util.*;

/**
 * Collection of mealy machine automata related methods.
 */
public class MealyUtils extends AutomatonUtils{

    /**
     * Provides all the outputs a mealy machine automaton can generate in
     * response to the given inputs.
     *
     * @param automaton         the mealy machine automaton to be used
     * @param inputs            the inputs of the automaton to be used
     * @param reachableOutputs  the modifiable collection to be used for storing
     *                          the reachable outputs
     */
    public static <S,I,O> void reachableOutputs(
        MealyMachine<S, I, ?, O> automaton, Collection<I> inputs,
        Collection<O> reachableOutputs) {

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
