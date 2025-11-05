package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automaton.transducer.MealyMachine;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;


/**
 * Collection of Mealy Machine automata related methods.
 */
public class MealyUtils extends AutomatonUtils {

    /**
     * Constructor
     */
    public MealyUtils() { }

    /**
     * Provides all the outputs a Mealy Machine automaton can generate in
     * response to the given inputs.
     *
     * @param <S>               the type of states
     * @param <I>               the type of inputs
     * @param <O>               the type of outputs
     *
     * @param automaton         the Mealy Machine automaton to be used
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
