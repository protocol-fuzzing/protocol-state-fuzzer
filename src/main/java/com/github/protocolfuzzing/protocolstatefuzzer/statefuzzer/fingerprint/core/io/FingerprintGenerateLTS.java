package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintLTS;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Converts Mealy machines to a combined suspension automaton.
 * <p>
 * Per-transition mid-state encoding
 * For every Mealy transition q -i/o-> r a fresh intermediate state q_i is
 * introduced, giving:
 *
 * <pre>
 *   q  --i-->  q_i  --o-->  r
 * </pre>
 *
 * Only the original (input-ready) states get a δ self-loop to avoid blocking.
 * Mid-states have exactly one outgoing transition (their output) and are
 * never blocking because the suspension-automaton check only requires that
 * every state has at least one enabled output — which the δ loop on the
 * source state satisfies for the source, and the output transition satisfies
 * for the mid-state.
 * <p>
 * Combining multiple models
 * All models are merged into one flat automaton: their state spaces are simply
 * concatenated with index offsets. No extra states or edges are added between
 * models. The initial state of the combined automaton is the initial state of
 * the first model. This matches the adg-finder input convention.
 * <p>
 * Alphabet
 * A shared alphabet is built from all models together so label indices are
 * consistent across the combined automaton. δ is appended last.
 */
public class FingerprintGenerateLTS {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Dummy output δ so that input states are non-blocking */
    public static final String QUIESCENCE = "delta";

    private List<String> inputs;
    private List<String> outputs;
    private Map<String, Integer> inputIndex;
    private Map<String, Integer> outputIndex;

    /**
     * Constructs a new instance of an LTS generator with the given alphabet
     *
     * @param inputs  the input alphabet as Strings
     * @param outputs the output alphabet as Strings
     */
    public FingerprintGenerateLTS(Collection<String> inputs, Collection<String> outputs) {
        this.inputs = new ArrayList<>(new TreeSet<>(inputs));
        this.outputs = new ArrayList<>(new TreeSet<>(outputs));
        this.inputIndex = new LinkedHashMap<>();
        this.outputIndex = new LinkedHashMap<>();
        for (int i = 0; i < this.inputs.size(); i++)
            inputIndex.put(this.inputs.get(i), i);
        for (int i = 0; i < this.outputs.size(); i++)
            outputIndex.put(this.outputs.get(i), inputs.size() + i);
    }

    /** Default constructor */
    public FingerprintGenerateLTS() {
        this.inputs = null;
        this.outputs = null;
        this.inputIndex = null;
        this.outputIndex = null;
    }

    /**
     * Number of inputs
     *
     * @return returns the number of distinct inputs
     */
    public int numInputs() {
        return inputs.size();
    }

    /**
     * Number of outputs
     *
     * @return returns the number of distinct outputs
     */
    public int numOutputs() {
        return outputs.size();
    }

    /**
     * List of inputs
     *
     * @return the list of distinct inputs as unmodifiable list
     */
    public List<String> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    /**
     * List of outputs
     *
     * @return the list of distinct outputs as unmodifiable list
     */
    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    /**
     * Decode an integer label to its string name.
     *
     * @param  label the integer label of a symbol
     *
     * @return       the String name of that symbol
     */
    public String labelName(int label) {
        if (label < numInputs())
            return inputs.get(label);
        int oi = label - numInputs();
        return oi < numOutputs() ? outputs.get(oi) : "label" + label;
    }

    /**
     * Encode a string name to an integer
     *
     * @param  label the String name of a symbol
     *
     * @return       the integer label of that symbol
     */
    public int labelIndex(String label) {
        if (inputIndex.containsKey(label))
            return inputIndex.get(label);
        if (outputIndex.containsKey(label))
            return outputIndex.get(label);
        throw new IllegalArgumentException("Unknown label: " + label);
    }

    /**
     * Builds the alphabet of the combined LTS
     *
     * @param  machines the list of MealyMachines with their alphabets ({@link MealyMachineWrapper})
     *                      from which the LTS will be made
     *
     * @return          the input and output sets in a list
     */
    public static List<Set<String>> buildAlphabet(List<MealyMachineWrapper<String, String>> machines) {
        Set<String> ins = new TreeSet<>();
        Set<String> outs = new TreeSet<>();
        int size = machines.size();

        for (int m = 0; m < size; m++) {
            for (String i: machines.get(m).getAlphabet()) {
                ins.add(i);
                for (MealyTransition tr: mealyTransitions(machines.get(m).getMealyMachine(), i)) {
                    outs.add(tr.output);
                }
            }
        }

        ins.add("reset");
        outs.add("");
        outs.add(QUIESCENCE);
        List<Set<String>> result = new ArrayList<>();
        result.add(ins);
        result.add(outs);
        return result;
    }

    /**
     * Return the number of transitions in a MealyMachine
     *
     * @param  <S>           the state type of the Machine
     * @param  machine       the Mealy Machine
     * @param  inputAlphabet the input Alphabet of the Machinr
     *
     * @return               the number of transitions
     */
    private static <S> int numberTransitions(MealyMachine<S, String, ?, String> machine,
        Alphabet<String> inputAlphabet) {
        int count = 0;
        for (S s: machine.getStates()) {
            for (String i: inputAlphabet) {
                try {
                    count += machine.getTransitions(s, i) != null ? machine.getTransitions(s, i).size() : 0;
                }
                catch (Exception e) {
                    LOGGER.error("Error while getting transition");
                }
            }
        }
        return count;
    }

    /**
     * Returns the transitions enabled by a specific input symbol in a Mealy Machine
     *
     * @param  <S>     the state type of the Machine
     * @param  machine the Mealy Machine
     * @param  i       the input symbol
     *
     * @return         the list of transitions enabled by {@code i} as a {@link MealyTransition}
     */
    private static <S> Set<MealyTransition> mealyTransitions(MealyMachine<S, String, ?, String> machine, String i) {
        Set<MealyTransition> result = new LinkedHashSet<>();
        for (S s: machine.getStates()) {
            try {
                if (machine.getTransitions(s, i) == null)
                    continue;
                if (machine.getSuccessor(s, i) != null
                    && machine.getOutput(s, i) != null) {
                    Object src = s;
                    Object dst = machine.getSuccessor(s, i);
                    if (src == null || dst == null)
                        continue;
                    result.add(new MealyTransition(src.toString(), i, machine.getOutput(s, i), dst.toString()));
                }
            }
            catch (Exception e) {
                LOGGER.error("Error while getting transition");
            }

        }

        return result;
    }

    /**
     * Convert all models and merge them into a single suspension automaton.
     * <p>
     * State layout (flat):
     *
     * <pre>
     *   model 0:  [orig_0 | mid_0]   indices [off[0] .. off[1]-1]
     *   model 1:  [orig_1 | mid_1]   indices [off[1] .. off[2]-1]
     *   ...
     * </pre>
     *
     * The combined initial state = model 0's initial state (at off[0] + its index).
     * No connections are added between models.
     *
     * @param  machines   parsed Mealy machines (same order as modelNames)
     * @param  modelNames names for each machine (used for leaf annotation)
     *
     * @return            combined automaton plus metadata
     */
    public CombinedLTS combine(List<MealyMachineWrapper<String, String>> machines,
        List<String> modelNames) {

        if (this.inputs == null || this.outputs == null || this.inputIndex == null || this.outputIndex == null) {
            List<Set<String>> alphabet = buildAlphabet(machines);
            this.inputs = new ArrayList<>(new TreeSet<>(alphabet.get(0)));
            this.outputs = new ArrayList<>(new TreeSet<>(alphabet.get(1)));
            inputIndex = new LinkedHashMap<>();
            outputIndex = new LinkedHashMap<>();
            for (int i = 0; i < this.inputs.size(); i++)
                inputIndex.put(this.inputs.get(i), i);
            for (int i = 0; i < this.outputs.size(); i++)
                outputIndex.put(this.outputs.get(i), numInputs() + i);

        }

        if (machines.isEmpty())
            throw new IllegalArgumentException("No machines");

        int size = machines.size();

        int delta = outputIndex.get(QUIESCENCE);
        int L = numInputs() + numOutputs();

        // Compute per-model state counts
        int[] nOrig = new int[size];
        int[] nMid = new int[size]; // one mid-state per transition
        for (int m = 0; m < size; m++) {
            nOrig[m] = machines.get(m).getMealyMachine().getStates().size();
            nMid[m] = numberTransitions(machines.get(m).getMealyMachine(), machines.get(m).getAlphabet()) + nOrig[m]; // +
                                                                                                                      // reset
                                                                                                                      // edge
                                                                                                                      // from
                                                                                                                      // all
                                                                                                                      // original
                                                                                                                      // states
                                                                                                                      // to
                                                                                                                      // initial
                                                                                                                      // state
        }

        // Flat offsets
        int[] off = new int[size];
        off[0] = 0;
        for (int m = 1; m < size; m++)
            off[m] = off[m - 1] + nOrig[m - 1] + nMid[m - 1];

        int totalStates = off[size - 1] + nOrig[size - 1] + nMid[size - 1];

        int[][] T = new int[totalStates][L];
        for (int[] row: T)
            Arrays.fill(row, -1);

        // stateToModel[s] = model index that state s belongs to
        int[] stateToModel = new int[totalStates];
        Arrays.fill(stateToModel, -1);

        int[] modelInitials = new int[machines.size()];
        List<Map<String, Integer>> stateIndexMaps = new ArrayList<>();

        for (int m = 0; m < machines.size(); m++) {
            MealyMachine<?, String, ?, String> machine = machines.get(m).getMealyMachine();
            int baseOrig = off[m];
            int baseMid = off[m] + nOrig[m];

            // Map state names → flat indices
            Map<String, Integer> si = new LinkedHashMap<>();
            for (Object s: machine.getStates()) {
                int idx = baseOrig + si.size();
                si.put(s.toString(), idx);
                stateToModel[idx] = m;
                T[idx][delta] = idx; // δ self-loop on every original (input-ready) state
            }

            stateIndexMaps.add(si);
            Object is = machine.getInitialState();
            if (is == null)
                modelInitials[m] = baseOrig;
            else
                modelInitials[m] = si.get(is.toString());

            // Per-transition mid-state: q -i-> q_i -o-> r
            int t = 0;
            for (String i: machines.get(m).getAlphabet()) {
                for (MealyTransition tr: mealyTransitions(machine, i)) {
                    int src = si.get(tr.src);
                    int inLabel = inputIndex.get(tr.input);
                    int outLabel = outputIndex.get(tr.output);
                    int dst = si.get(tr.dst);
                    int mid = baseMid + t;

                    stateToModel[mid] = m;
                    // Mid-states have only their output transition (no δ).
                    // This makes output mismatches visible to Algorithm 1.
                    T[src][inLabel] = mid;
                    T[mid][outLabel] = dst;
                    t++;
                }
            }

            // Add reset edges to initial state
            int dst = modelInitials[m];
            int inLabel = inputIndex.get("reset");
            int outLabel = outputIndex.get("");
            for (Object s: machine.getStates()) {
                int src = si.get(s.toString());
                int mid = baseMid + t;

                stateToModel[mid] = m;
                T[src][inLabel] = mid;
                T[mid][outLabel] = dst;
                t++;
            }
        }

        // Initial state of the combined automaton = model 0's initial state
        int combinedInit = modelInitials[0];
        FingerprintLTS automaton = new FingerprintLTS(totalStates, numInputs(), numOutputs(), T, combinedInit);

        return new CombinedLTS(automaton, stateToModel, modelInitials,
            modelNames, off, nOrig, nMid, stateIndexMaps);
    }

    // ── Result ────────────────────────────────────────────────────────────────

    /** Contains the combined LTS with metadata */
    public static class CombinedLTS {
        /** The combined LTS automaton */
        public final FingerprintLTS automaton;
        /** stateToModel[s] = index of the model owning state s, or -1. */
        public final int[] stateToModel;
        /** Flat index of each model's initial state. */
        public final int[] modelInitials;
        /** The names of representative models */
        public final List<String> modelNames;
        /** The offset of each models states in the combined LTS */
        public final int[] offsets;
        /** The number of original states of each models */
        public final int[] origCounts;
        /** The number of middle states of each model */
        public final int[] midCounts;
        /** The map of original state names */
        public final List<Map<String, Integer>> stateIndexMaps;

        /**
         * Constucts a new instance of a combined LTS
         *
         * @param a     the LTS automaton
         * @param stm   map of state indices to model indices
         * @param mi    list of model initial states
         * @param mn    list of model names
         * @param off   offset of each model states in the combined
         * @param nOrig number of original states in each model
         * @param nMid  number of middle states in each model
         * @param sim   list of maps of original state names to indices for each model
         */
        public CombinedLTS(FingerprintLTS a, int[] stm, int[] mi, List<String> mn,
            int[] off, int[] nOrig, int[] nMid,
            List<Map<String, Integer>> sim) {
            this.automaton = a;
            this.stateToModel = stm;
            this.modelInitials = mi;
            this.modelNames = mn;
            this.offsets = off;
            this.origCounts = nOrig;
            this.midCounts = nMid;
            this.stateIndexMaps = sim;
        }

        /**
         * Return the model name owning state s, or null for unowned states.
         *
         * @param  s the index of the state
         *
         * @return   the name of the model owning state s, or null for unowned states
         */
        public String modelOf(int s) {
            int m = stateToModel[s];
            return m >= 0 ? modelNames.get(m) : null;
        }

        /**
         * Given a set of states, return the set of model names represented.
         *
         * @param  states the set of state indices
         *
         * @return        the set of model names owning those states
         */
        public Set<String> modelsIn(Set<Integer> states) {
            Set<String> result = new LinkedHashSet<>();
            for (int s: states) {
                String mn = modelOf(s);
                if (mn != null)
                    result.add(mn);
            }
            return result;
        }

        /**
         * Given a set of states, return the set of initial states for the
         * models that own those states.
         *
         * @param  states the set of state indices
         *
         * @return        the set of initial states for the models that own those states
         */
        public Set<Integer> initialStates(Set<Integer> states) {
            Set<Integer> result = new LinkedHashSet<>();
            Set<Integer> seenModels = new HashSet<>();
            for (int s: states) {
                int m = stateToModel[s];
                if (m >= 0 && seenModels.add(m)) {
                    result.add(modelInitials[m]);
                }
            }
            return result;
        }

        /**
         * Returns true if state s is an original (non-mid) state of any model.
         * Original states occupy indices [offsets[m] .. offsets[m]+origCounts[m])
         * for each model m. Mid-states follow immediately after.
         *
         * @param  s the index of the state
         *
         * @return   true if state s is an original (non-mid) state of any model
         */
        public boolean isOriginalState(int s) {
            int m = stateToModel[s];
            if (m < 0)
                return false;
            return s < offsets[m] + origCounts[m];
        }

        /**
         * Filter a state set to only original (non-mid) states.
         *
         * @param  states the set of state indices
         *
         * @return        the set of original (non-mid) state indices
         */
        public Set<Integer> originalStatesOnly(Set<Integer> states) {
            Set<Integer> result = new LinkedHashSet<>();
            for (int s: states)
                if (isOriginalState(s))
                    result.add(s);
            return result;
        }

        /**
         * Return the set of initial states
         *
         * @return the initial States
         */
        public Set<Integer> initialStates() {
            Set<Integer> result = new LinkedHashSet<>();
            for (int mi: modelInitials)
                result.add(mi);
            return result;
        }
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
