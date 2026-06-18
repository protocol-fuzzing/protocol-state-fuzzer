package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A simple representation of an LTS automaton
 * <p>
 * This representation uses ints for states and
 * transitions to simplify the algorithms *
 */
public class FingerprintLTS {

    /** Number of states */
    private final int numStates;
    /** Number of input symbols */
    private final int numInputs;
    /** Number of output symbols */
    private final int numOutputs;
    /** transition[state][label] = successor, or -1 if undefined */
    private final int[][] transition;
    /** Initial state of the LTS */
    private final int initialState;

    /** Cache: for each state, the set of enabled inputs */
    private final Set<Integer>[] inputsCache;
    /** Cache: for each state, the set of enabled outputs */
    private final Set<Integer>[] outputsCache;
    /** Cache: for each state, the set of all enabled labels */
    private final Set<Integer>[] labelsCache;

    /**
     * Public constructor for an LTS
     *
     * @param numStates    the number of states that the LTS will have
     * @param numInputs    the number of different input symbols
     * @param numOutputs   the number of different output symbols
     * @param transition   a 2D array representing the transition field1 -> starting state
     *                         field2 -> input or output
     *                         returns the final state
     * @param initialState the initial state of the automaton
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public FingerprintLTS(int numStates, int numInputs, int numOutputs, int[][] transition, int initialState) {
        this.numStates = numStates;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.transition = transition;
        this.initialState = initialState;

        this.inputsCache = new Set[numStates];
        this.outputsCache = new Set[numStates];
        this.labelsCache = new Set[numStates];

        for (int q = 0; q < numStates; q++) {
            Set<Integer> ins = new LinkedHashSet<>();
            Set<Integer> outs = new LinkedHashSet<>();
            for (int a = 0; a < numInputs; a++) {
                if (transition[q][a] >= 0)
                    ins.add(a);
            }
            for (int x = numInputs; x < numInputs + numOutputs; x++) {
                if (transition[q][x] >= 0)
                    outs.add(x);
            }
            inputsCache[q] = Collections.unmodifiableSet(ins);
            outputsCache[q] = Collections.unmodifiableSet(outs);
            Set<Integer> all = new LinkedHashSet<>(ins);
            all.addAll(outs);
            labelsCache[q] = Collections.unmodifiableSet(all);
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    /**
     * Getter
     *
     * @return the number of States
     */
    public int getNumStates() {
        return numStates;
    }

    /**
     * Getter
     *
     * @return the number of Inputs symbols
     */
    public int getNumInputs() {
        return numInputs;
    }

    /**
     * Getter
     *
     * @return the number of Output symbols
     */
    public int getNumOutputs() {
        return numOutputs;
    }

    /**
     * Getter
     *
     * @return the number of Inputs + Output symbols
     */
    public int getNumLabels() {
        return numInputs + numOutputs;
    }

    /**
     * Getter
     *
     * @return the initial State
     */
    public int getInitialState() {
        return initialState;
    }

    /**
     * Returns true if the symbol is input, false if output or out of bounds
     *
     * @param  label the int index of a transition symbol
     *
     * @return       true if the symbol is input, false if output or out of bounds
     */
    public boolean isInput(int label) {
        return label >= 0 && label < numInputs;
    }

    /**
     * Returns true if the symbol is output, false if input or out of bounds
     *
     * @param  label the int index of a transition symbol
     *
     * @return       true if the symbol is output, false if input or out of bounds
     */
    public boolean isOutput(int label) {
        return label >= numInputs && label < numInputs + numOutputs;
    }

    /**
     * Returns the successor of state q on label mu, or -1 if undefined.
     *
     * @param  q  State q
     * @param  mu transition symbol
     *
     * @return    the successor of state q on label mu, or -1 if undefined.
     */
    public int transition(int q, int mu) {
        if (q < 0 || q >= numStates || mu < 0 || mu >= numInputs + numOutputs)
            return -1;
        return transition[q][mu];
    }

    /**
     * T(q, mu) is defined
     *
     * @param  q  State q
     * @param  mu transition symbol
     *
     * @return    true if the transition T(q, mu) is defined, false if not
     */
    public boolean defined(int q, int mu) {
        return transition(q, mu) >= 0;
    }

    /**
     * Returns the enabled input symbols from state {@code q}.
     * <p>
     * The returned set contains the indices of all input labels for which a
     * transition is defined from {@code q}.
     *
     * @param  q the source state
     *
     * @return   an unmodifiable set of enabled input label indices for {@code q}
     */
    public Set<Integer> in(int q) {
        return inputsCache[q];
    }

    /**
     * Returns the enabled output symbols from state {@code q}.
     * <p>
     * The returned set contains the indices of all output labels for which a
     * transition is defined from {@code q}.
     *
     * @param  q the source state
     *
     * @return   an unmodifiable set of enabled output label indices for {@code q}
     */
    public Set<Integer> out(int q) {
        return outputsCache[q];
    }

    /**
     * Returns all enabled transition labels from state {@code q}.
     * <p>
     * The returned set contains both input and output label indices for which
     * a transition is defined from {@code q}.
     *
     * @param  q the source state
     *
     * @return   an unmodifiable set of enabled label indices for {@code q}
     */
    public Set<Integer> labels(int q) {
        return labelsCache[q];
    }

    /**
     * Computes the union of enabled input symbols from a set of states.
     *
     * @param  states a collection of states
     *
     * @return        the set of input label indices enabled in at least one state in {@code states}
     */
    public Set<Integer> in(Set<Integer> states) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int q: states)
            result.addAll(in(q));
        return result;
    }

    /**
     * Computes the union of enabled output symbols from a set of states.
     *
     * @param  states a collection of states
     *
     * @return        the set of output label indices enabled in at least one state in {@code states}
     */
    public Set<Integer> out(Set<Integer> states) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int q: states)
            result.addAll(out(q));
        return result;
    }

    /**
     * Whether state q is blocking (no outgoing output transitions)
     *
     * @param  q the state to check
     *
     * @return   true if q is blocking (has no output enabled), false otherwise
     */
    public boolean isBlocking(int q) {
        return outputsCache[q].isEmpty();
    }

    /**
     * Whether this automaton is a suspension automaton (no blocking states)
     *
     * @return true is the automataton is a Suspension automaton (every state is non-blocking), false otherwise
     */
    public boolean isSuspensionAutomaton() {
        for (int q = 0; q < numStates; q++) {
            if (isBlocking(q))
                return false;
        }
        return true;
    }

    /**
     * Computes the set of states reachable from any state in {@code P} by following
     * the trace {@code sigma}.
     * <p>
     * Each label in {@code sigma} is applied sequentially to the current set of
     * states, and only defined transitions contribute to the next reachable set.
     *
     * @param  P     the starting set of states
     * @param  sigma the trace to follow
     *
     * @return       the set of states reachable after executing {@code sigma} from any state in {@code P}
     */
    public Set<Integer> after(Set<Integer> P, int[] sigma) {
        Set<Integer> current = new LinkedHashSet<>(P);
        for (int mu: sigma) {
            Set<Integer> next = new LinkedHashSet<>();
            for (int q: current) {
                int succ = transition(q, mu);
                if (succ >= 0)
                    next.add(succ);
            }
            current = next;
        }
        return current;
    }

    /**
     * Computes the set of states reachable from any state in {@code P} by a single
     * transition on label {@code mu}.
     *
     * @param  P  the starting set of states
     * @param  mu the transition label
     *
     * @return    the set of successor states reached from {@code P} via {@code mu}
     */
    public Set<Integer> after(Set<Integer> P, int mu) {
        return after(P, new int[] {mu});
    }

    /**
     * Computes the set of states reachable from a single source state {@code q}
     * by following the trace {@code sigma}.
     *
     * @param  q     the starting state
     * @param  sigma the trace to follow
     *
     * @return       the set of states reachable after executing {@code sigma} from {@code q}
     */
    public Set<Integer> after(int q, int[] sigma) {
        return after(Collections.singleton(q), sigma);
    }

    /**
     * Computes the subset of {@code P} from which the trace {@code sigma} is
     * enabled.
     * <p>
     * A state is included if there exists a defined path labeled by {@code sigma}
     * starting from that state.
     *
     * @param  P     the candidate set of states
     * @param  sigma the trace whose enablement is checked
     *
     * @return       the subset of {@code P} from which {@code sigma} can be executed
     */
    public Set<Integer> enabled(Set<Integer> P, int[] sigma) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int q: P) {
            if (!after(Collections.singleton(q), sigma).isEmpty())
                result.add(q);
        }
        return result;
    }

    /**
     * Computes the subset of {@code P} from which the single transition {@code mu}
     * is enabled.
     *
     * @param  P  the candidate set of states
     * @param  mu the transition label whose enablement is checked
     *
     * @return    the subset of {@code P} from which {@code mu} is defined
     */
    public Set<Integer> enabled(Set<Integer> P, int mu) {
        return enabled(P, new int[] {mu});
    }

    /**
     * Computes the set of states that can reach some state in {@code P} by taking a
     * single transition on label {@code mu}.
     * <p>
     * This is computed by checking all states in the automaton for a defined
     * transition on {@code mu} that leads into {@code P}.
     *
     * @param  P  the target set of states
     * @param  mu the transition label
     *
     * @return    the set of predecessor states that can reach {@code P} via {@code mu}
     */
    public Set<Integer> before(Set<Integer> P, int mu) {
        // Build reverse index (lazily? – precompute for efficiency)
        Set<Integer> result = new LinkedHashSet<>();
        for (int q = 0; q < numStates; q++) {
            if (P.contains(transition(q, mu)))
                result.add(q);
        }
        return result;
    }

    /**
     * Encodes a pair of states (q1, q2) from two automata into a single state index for the composed automaton.
     *
     * @param  q1 the state from the first automaton
     * @param  q2 the state from the second automaton
     * @param  n2 the number of states in the second automaton
     *
     * @return    the encoded state index representing the pair (q1, q2) in the composed automaton
     */
    public static int encode(int q1, int q2, int n2) {
        return q1 * n2 + q2;
    }

    /**
     * Decodes the first component (q1) of a state index from the composed automaton.
     *
     * @param  q  the state index from the composed automaton
     * @param  n2 the number of states in the second automaton
     *
     * @return    the decoded state from the first automaton
     */
    public static int decodeFirst(int q, int n2) {
        return q / n2;
    }

    /**
     * Decodes the second component (q2) of a state index from the composed automaton.
     *
     * @param  q  the state index from the composed automaton
     * @param  n2 the number of states in the second automaton
     *
     * @return    the decoded state from the second automaton
     */
    public static int decodeSecond(int q, int n2) {
        return q % n2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Automaton(states=").append(numStates)
            .append(", inputs=").append(numInputs)
            .append(", outputs=").append(numOutputs)
            .append(", init=").append(initialState).append(")\n");
        for (int q = 0; q < numStates; q++) {
            for (int mu = 0; mu < getNumLabels(); mu++) {
                int s = transition(q, mu);
                if (s >= 0) {
                    sb.append("  ").append(q)
                        .append(" --").append(isInput(mu) ? "i" : "o").append(mu).append("--> ")
                        .append(s).append("\n");
                }
            }
        }
        return sb.toString();
    }

}
