package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Decides behavioural equivalence of two {@link MealyMachine}s.
 * Build an explicit transition function: index states and map
 * every (state, input) pair to an (output, successor) pair.
 * <p>
 * Partition-refinement minimization: (Mealy variant of Hopcroft's
 * algorithm) compute the coarsest stable partition where two states are
 * in the same class iff they are observationally equivalent. States that
 * are unreachable from the initial state are pruned first.
 * <p>
 * Canonical BFS serialisation: renumber the minimised states in
 * BFS order starting from the initial-state class, then serialise the
 * transition table to a {@code String}. Two machines are equivalent iff
 * their canonical strings are equal.
 */
public final class FingerprintMealyEquivalence {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Default constructore
     */
    private FingerprintMealyEquivalence() {}

    /**
     * Returns {@code true} iff {@code a} and {@code b} define the same
     * input/output behaviour (up to state renaming).
     *
     * @param  <I> the input type of the machines
     * @param  <O> the output type of the machines
     * @param  a   the first machine
     * @param  b   the second machine
     *
     * @return     {@code true} iff {@code a} and {@code b} define the same
     *                 input/output behaviour (up to state renaming).
     */
    public static <I, O> boolean equivalent(MealyMachineWrapper<I, O> a,
        MealyMachineWrapper<I, O> b) {
        return canonicalSignature(a).equals(canonicalSignature(b));
    }

    /**
     * Computes a canonical string that is identical for behaviourally
     * equivalent machines and different for non-equivalent ones.
     * <p>
     * The string encodes the minimised, BFS-ordered transition table:
     * {@code "INPUTS:<i0>,<i1>,...;TRANSITIONS:<state>:<input>=<output>-><dst>;..."}
     *
     * @param  <I> the input type of m
     * @param  <O> the output type of m
     * @param  m   a {@link MealyMachine} (not modified)
     *
     * @return     canonical signature string
     */
    public static <I, O> String canonicalSignature(MealyMachineWrapper<I, O> m) {
        MealyIndex<?, I, O> idx = new MealyIndex<>(m.getMealyMachine(), m.getAlphabet());
        if (idx.initState < 0)
            return "<empty>";

        int[] partition = minimise(idx);
        return serialise(idx, partition);
    }

    /** Integer-indexed view of a MealyMachine, reachable states only. */
    private static final class MealyIndex<S, I, O> {

        /** Sorted list of all input symbols seen on reachable transitions. */
        final String[] inputs;

        /** Number of reachable states. */
        final int numStates;

        /** Index of the initial state (always 0 after reachability pruning). */
        final int initState;

        /**
         * {@code trans[s][i]} = int-packed (output-index << 16 | dest-state).
         * -1 means the (state, input) pair has no transition (incomplete machine).
         */
        final int[][] trans; // [state][inputIdx] -> packed(outIdx, dst)

        /** Output symbols, indexed. */
        final String[] outputs;

        /**
         * Constructor for the MealyIndex
         *
         * @param m        the Mealy Machine
         * @param alphabet the alphabet for the Machine
         */
        MealyIndex(MealyMachine<S, I, ?, O> m, Alphabet<I> alphabet) {
            Map<String, Integer> stateIdx = new LinkedHashMap<>();
            for (S s: m.getStates())
                stateIdx.put(s.toString(), stateIdx.size());

            S is = m.getInitialState();
            int rawInit = is != null
                ? stateIdx.getOrDefault(is.toString(), -1) : -1;

            Set<String> inputSet = new TreeSet<>();
            Set<String> outputSet = new TreeSet<>();
            Set<MealyTransition> T = mealyTransitions(m, alphabet);
            for (MealyTransition t: T) {
                inputSet.add(t.input);
                outputSet.add(t.output);
            }

            String[] rawInputs = inputSet.toArray(new String[0]);
            String[] rawOutputs = outputSet.toArray(new String[0]);
            Map<String, Integer> inputIdx = index(rawInputs);
            Map<String, Integer> outputIdx = index(rawOutputs);

            int n = stateIdx.size();
            int k = rawInputs.length;
            int[][] rawTrans = new int[n][k];
            for (int[] row: rawTrans)
                Arrays.fill(row, -1);

            for (MealyTransition t: T) {
                int s = stateIdx.getOrDefault(t.src, -1);
                int i = inputIdx.getOrDefault(t.input, -1);
                int o = outputIdx.getOrDefault(t.output, -1);
                int d = stateIdx.getOrDefault(t.dst, -1);
                if (s < 0 || i < 0 || o < 0 || d < 0)
                    continue;
                rawTrans[s][i] = pack(o, d);
            }

            if (rawInit < 0) {
                this.numStates = 0;
                this.initState = -1;
                this.inputs = rawInputs;
                this.outputs = rawOutputs;
                this.trans = new int[0][0];
                return;
            }

            boolean[] reachable = new boolean[n];
            Queue<Integer> queue = new ArrayDeque<>();
            reachable[rawInit] = true;
            queue.add(rawInit);
            while (!queue.isEmpty()) {
                int s = queue.poll();
                for (int i = 0; i < k; i++) {
                    int packed = rawTrans[s][i];
                    if (packed < 0)
                        continue;
                    int d = dst(packed);
                    if (!reachable[d]) {
                        reachable[d] = true;
                        queue.add(d);
                    }
                }
            }

            int[] remap = new int[n];
            Arrays.fill(remap, -1);
            remap[rawInit] = 0;
            int cnt = 1;
            for (int s = 0; s < n; s++) {
                if (reachable[s] && s != rawInit)
                    remap[s] = cnt++;
            }

            this.numStates = cnt;
            this.initState = 0;
            this.inputs = rawInputs;
            this.outputs = rawOutputs;
            this.trans = new int[cnt][k];
            for (int[] row: this.trans)
                Arrays.fill(row, -1);
            for (int s = 0; s < n; s++) {
                if (!reachable[s])
                    continue;
                int ns = remap[s];
                for (int i = 0; i < k; i++) {
                    int packed = rawTrans[s][i];
                    if (packed < 0)
                        continue;
                    int d = dst(packed);
                    if (remap[d] < 0)
                        continue; // unreachable dst (shouldn't happen)
                    this.trans[ns][i] = pack(out(packed), remap[d]);
                }
            }
        }

        private static Map<String, Integer> index(String[] arr) {
            Map<String, Integer> m = new HashMap<>();
            for (int i = 0; i < arr.length; i++)
                m.put(arr[i], i);
            return m;
        }
    }

    /**
     * Returns an array {@code partition[s]} = class index for each state {@code s}.
     * States in the same class are observationally equivalent.
     * </p>
     * Initial partition: group states by their full output signature
     * (output vector over all inputs, using -1 for missing transitions).
     * Then iteratively split classes whose successors land in different classes.
     *
     * @param  idx the {@code MealyIndex} t
     *
     * @return     an array {@code partition[s]} = class index for each state {@code s}
     */
    private static int[] minimise(MealyIndex<?, ?, ?> idx) {
        int n = idx.numStates;
        int k = idx.inputs.length;

        if (n == 0)
            return new int[0];

        int[] partition = new int[n];
        {
            Map<String, Integer> sigToClass = new HashMap<>();
            for (int s = 0; s < n; s++) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < k; i++) {
                    int p = idx.trans[s][i];
                    sb.append(p < 0 ? -1 : out(p)).append(',');
                }
                String sig = sb.toString();
                partition[s] = sigToClass.computeIfAbsent(sig, x -> sigToClass.size());
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            Map<String, Integer> sigToClass = new HashMap<>();
            int[] newPartition = new int[n];
            for (int s = 0; s < n; s++) {
                StringBuilder sb = new StringBuilder();
                sb.append(partition[s]).append('|');
                for (int i = 0; i < k; i++) {
                    int p = idx.trans[s][i];
                    if (p < 0) {
                        sb.append("-1,-1,");
                        continue;
                    }
                    sb.append(out(p)).append(',').append(partition[dst(p)]).append(',');
                }
                String sig = sb.toString();
                newPartition[s] = sigToClass.computeIfAbsent(sig, x -> sigToClass.size());
            }
            if (!Arrays.equals(partition, newPartition)) {
                partition = newPartition;
                changed = true;
            }
        }
        return partition;
    }

    /**
     * Serialises the minimised machine in a canonical BFS order.
     * </p>
     * Classes are numbered in the order they are first reached by a BFS
     * starting from the initial-state class, traversing inputs in sorted order.
     * This guarantees that two equivalent machines produce identical strings
     * regardless of the original state naming.
     *
     * @param  idx       the {@code MealyIndex}
     * @param  partition matches states to classes
     *
     * @return           the String resulting from the serialisation
     */
    private static String serialise(MealyIndex<?, ?, ?> idx, int[] partition) {
        if (idx.numStates == 0)
            return "<empty>";

        int k = idx.inputs.length;
        int initClass = partition[idx.initState];

        // Pick one representative state per class
        int numClasses = Arrays.stream(partition).max().orElse(0) + 1;
        int[] rep = new int[numClasses];
        Arrays.fill(rep, -1);
        for (int s = 0; s < idx.numStates; s++) {
            if (rep[partition[s]] < 0)
                rep[partition[s]] = s;
        }

        // BFS over classes, assign canonical IDs in visit order
        int[] canonicalId = new int[numClasses];
        Arrays.fill(canonicalId, -1);
        canonicalId[initClass] = 0;
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(initClass);
        int nextId = 1;
        // We need a stable visit order → process in BFS + input-sorted order
        List<Integer> bfsOrder = new ArrayList<>();
        bfsOrder.add(initClass);

        while (!queue.isEmpty()) {
            int cls = queue.poll();
            int s = rep[cls];
            for (int i = 0; i < k; i++) {
                int p = idx.trans[s][i];
                if (p < 0)
                    continue;
                int dstClass = partition[dst(p)];
                if (canonicalId[dstClass] < 0) {
                    canonicalId[dstClass] = nextId++;
                    queue.add(dstClass);
                    bfsOrder.add(dstClass);
                }
            }
        }

        // Build the canonical string
        StringBuilder sb = new StringBuilder();
        sb.append("INPUTS:");
        sb.append(String.join(",", idx.inputs));
        sb.append(";TRANSITIONS:");

        for (int cls: bfsOrder) {
            int s = rep[cls];
            sb.append(canonicalId[cls]).append(':');
            for (int i = 0; i < k; i++) {
                int p = idx.trans[s][i];
                if (p < 0)
                    continue;
                sb.append(idx.inputs[i]).append('=')
                    .append(idx.outputs[out(p)]).append("->")
                    .append(canonicalId[partition[dst(p)]]).append(';');
            }
        }

        return sb.toString();
    }

    // Helpers – pack/unpack (outputIdx, destState) into one int

    private static int pack(int outIdx, int dst) {
        return (outIdx << 16) | (dst & 0xFFFF);
    }

    private static int out(int packed) {
        return packed >>> 16;
    }

    private static int dst(int packed) {
        return packed & 0xFFFF;
    }

    /**
     * Returns the transitions in a Mealy Machine
     *
     * @param  <S>      the state type of the Machine
     * @param  <I>      the input type of the Machine
     * @param  <O>      the output type of the Machine
     * @param  machine  the Mealy Machine
     * @param  alphabet the input alphabet of the machine
     *
     * @return          the list of transitions as a {@link MealyTransition}
     */
    private static <S, I, O> Set<MealyTransition> mealyTransitions(MealyMachine<S, I, ?, O> machine,
        Alphabet<I> alphabet) {
        Set<MealyTransition> result = new LinkedHashSet<>();
        for (S s: machine.getStates()) {
            for (I i: alphabet) {
                try {
                    if (machine.getTransitions(s, i) == null)
                        continue;
                    O output = machine.getOutput(s, i);
                    S succ = machine.getSuccessor(s, i);
                    if (output != null
                        && succ != null) {
                        S src = s;
                        S dst = succ;
                        result.add(new MealyTransition(src.toString(), i.toString(), output.toString(),
                            dst.toString()));
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Error while getting transition");
                }
            }

        }

        return result;
    }

    /**
     * Represents a transition in a Mealy Machine in a quatruple of strings
     */
    private static class MealyTransition {
        private final String src;
        private final String input;
        private final String output;
        private final String dst;

        /**
         * Constructs a new instance of a Mealy Transition
         *
         * @param src    the source of the transition
         * @param input  the input of the transition
         * @param output the output of the transition
         * @param dst    the destination state of the transition
         */
        private MealyTransition(String src, String input, String output, String dst) {
            this.src = src;
            this.input = input;
            this.output = output;
            this.dst = dst;
        }
    }
}
