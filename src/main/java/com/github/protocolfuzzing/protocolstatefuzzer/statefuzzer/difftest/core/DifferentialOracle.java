package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Performs differential testing of two Mealy machine models by exploring their
 * product state space using breadth-first search.
 * <p>
 * The analysis terminates when all reachable product state pairs have been explored.
 * <p>
 * When a divergence is found, the algorithm does not explore successors of the diverging state pair,
 * since the models already have shown different behaviour at that point.
 * This means divergences deeper in the state space may be missed
 * if they are only reachable through a diverging transition.
 * <p>
 * By default, outputs of the two models are compared using {@link Objects#equals}.
 * A custom {@link BiPredicate} can be provided to treat semantically equivalent outputs as equal.
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 */
public class DifferentialOracle<I, O> {

    /**
     * Defines the equivalence used to compare outputs from the two models.
     * Defaults to {@link Objects#equals} if not provided.
     */
    private final BiPredicate<O, O> outputEquivalence;

    /**
     * Constructs a new instance using strict output equality.
     * <p>
     * Uses {@link Objects#equals} to compare outputs, meaning any difference
     * in output values is recorded as a divergence.
     */
    public DifferentialOracle() {
        this(Objects::equals);
    }

    /**
     * Constructs a new instance using the given output equivalence.
     * <p>
     * Provides a custom equivalence check when some output difference should
     * be treated as semantically equivalent.
     *
     * @param outputEquivalence the predicate defining which output pairs
     *                              are considered equivalent
     */
    public DifferentialOracle(BiPredicate<O, O> outputEquivalence) {
        this.outputEquivalence = outputEquivalence;
    }

    /**
     * Analyses two Mealy machine models and returns all divergences found
     * <p>
     * For each reachable product state pair, every input symbol in the alphabet is tested.
     * A divergences is recorded when the two models produce different outputs for the same input,
     * or when one of the model has no transition defined where the other one does.
     * <p>
     * When a divergence is found for a given input, the successor state pair is
     * not explored further. Only transitions where both models agree on the output
     * cause further exploration.
     *
     * @param  <S1>     the state type of modelA
     * @param  <S2>     the state type of modelB
     * @param  modelA   the first Mealy machine model
     * @param  modelB   the second Mealy machine model
     * @param  alphabet the shared input alphabet used by both models
     *
     * @return          a list of divergence records, empty if the models behave
     *                      equivalent on all reachable states
     */
    public <S1, S2> List<DivergenceRecord<I, O>> analyse(
        MealyMachine<S1, I, ?, O> modelA,
        MealyMachine<S2, I, ?, O> modelB,
        Alphabet<I> alphabet) {

        List<DivergenceRecord<I, O>> divergences = new ArrayList<>();

        StatePair<S1, S2> initial = new StatePair<S1, S2>(modelA.getInitialState(), modelB.getInitialState());

        Queue<StatePair<S1, S2>> queue = new ArrayDeque<>();
        Set<StatePair<S1, S2>> visited = new HashSet<>();

        // maps each state pair to the pair that discovered it.
        // used for witness reconstruction
        Map<StatePair<?, ?>, Map.Entry<StatePair<?, ?>, I>> parentMap = new HashMap<>();

        queue.add(initial);
        visited.add(initial);

        while (!queue.isEmpty()) {
            StatePair<S1, S2> current = queue.poll();

            for (I input: alphabet) {
                O outputA = modelA.getOutput(current.stateA, input);
                O outputB = modelB.getOutput(current.stateB, input);

                if (outputA == null || outputB == null) {
                    if (outputA != outputB) {
                        List<I> witness = reconstructPath(parentMap, current);
                        divergences.add(new DivergenceRecord<>(witness, input, outputA, outputB));
                    }
                    continue;
                }

                if (!outputEquivalence.test(outputA, outputB)) {
                    List<I> witness = reconstructPath(parentMap, current);
                    divergences.add(new DivergenceRecord<>(witness, input, outputA, outputB));
                    continue;
                }

                StatePair<S1, S2> next = new StatePair<>(modelA.getSuccessor(current.stateA, input),
                    modelB.getSuccessor(current.stateB, input));

                if (!visited.contains(next)) {
                    visited.add(next);
                    parentMap.put(next, Map.entry(current, input));
                    queue.add(next);
                }
            }
        }
        return divergences;
    }

    /**
     * Reconstructs the witness sequence leading to the given state pair
     * by walking backwards through the parentMap.
     *
     * @param  parentMap maps each visited state pair to it parent pair and
     *                       the input that caused the transition
     * @param  current   the state pair to reconstruct the path to
     *
     * @return           the sequence of inputs leading to the given state pair,
     *                       empty if the state pair is the initial pair
     */
    private List<I> reconstructPath(Map<StatePair<?, ?>, Map.Entry<StatePair<?, ?>, I>> parentMap,
        StatePair<?, ?> current) {
        List<I> path = new ArrayList<>();
        StatePair<?, ?> node = current;

        while (parentMap.containsKey(node)) {
            Map.Entry<StatePair<?, ?>, I> entry = parentMap.get(node);
            path.add(entry.getValue());
            node = entry.getKey();
        }

        Collections.reverse(path);

        return path;
    }

    /**
     * Represents a pair of states, one from each model.
     *
     * @param <S1> the state type of the first model
     * @param <S2> the state type of the second model
     */
    private static class StatePair<S1, S2> {

        /** The state from the first model */
        final S1 stateA;

        /** The state from the second model */
        final S2 stateB;

        /**
         * Constructs a new instance for the given parameters
         *
         * @param stateA the state from the first model
         * @param stateB the state from the second model
         */
        StatePair(S1 stateA, S2 stateB) {
            this.stateA = stateA;
            this.stateB = stateB;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof StatePair))
                return false;
            StatePair<?, ?> other = (StatePair<?, ?>) o;
            return stateA.equals(other.stateA) && stateB.equals(other.stateB);
        }

        @Override
        public int hashCode() {
            return 31 * stateA.hashCode() + stateB.hashCode();
        }
    }

}
