package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintAutomaton;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintLTS;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintSplittingGraph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains the functions to calculate the Compatibility relation of
 * state pairs in an {@link FingerprintLTS}
 */
public class FingerprintComputeCompatibility {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Represents the tester's "no input" action θ */
    public static final int THETA = Integer.MAX_VALUE;

    /** Result record */
    public static class Result {
        /** Array indicating which states are invalid */
        public final boolean[] isInvalid;

        /** Default constructor, initializes the isInvalid array */
        Result(int numStates) {
            isInvalid = new boolean[numStates];
        }

    }

    /** Default constructor */
    public FingerprintComputeCompatibility() {}

    /**
     * For every state q = (q1, q2) of the composed automaton A = S||S,
     * where q1 and q2 are states of the LTS automaton S,
     * calculates whether it is a valid state.
     * The set of valid states of an automaton S
     * is the largest set P ⊆ Q such that q ∈ P implies
     * ∀a ∈ in(q) : T(q, a) ∈ P and ∃x ∈ out(q) : T(q, x) ∈ P.
     *
     * @param  S the LTS automaton S
     *
     * @return   the validity of states q of S||S
     */

    private static Result computeNoCompose(FingerprintLTS S) {
        int n = S.getNumStates();
        int nc = n * n; // number of states in S||S

        // count(q) = number of output successors of q not yet known to be invalid
        int[] count = new int[nc];
        int[] W = new int[nc]; // queue of states known to be invalid awaiting processing
        int wHead = 0, wTail = 0; // head/tail indices
        int[] incoming_count = new int[nc];
        int[] incoming_offset = new int[nc + 1];
        int transitions = 0;

        // ── Initialization ────────────────────────────────────────
        for (int q1 = 0; q1 < n; q1++) {
            for (int q2 = 0; q2 < n; q2++) {
                int q = FingerprintLTS.encode(q1, q2, n);
                for (int mu = 0; mu < S.getNumLabels(); mu++) {
                    int succ1 = S.transition(q1, mu);
                    int succ2 = S.transition(q2, mu);
                    if (succ1 >= 0 && succ2 >= 0) {
                        int succ = FingerprintLTS.encode(succ1, succ2, n);
                        if (mu >= S.getNumInputs()) {
                            count[q]++;
                        }
                        incoming_count[succ]++;
                        transitions++;
                    }
                }
            }
        }
        incoming_offset[nc] = transitions;
        incoming_offset[0] = 0;
        for (int i = 0; i < nc; i++) {
            incoming_offset[i + 1] = incoming_offset[i] + incoming_count[i];
        }

        int[] incoming_edge_q = new int[transitions];
        int[] incoming_edge_mu = new int[transitions];
        int[] incoming_edge_index = new int[nc];
        for (int q1 = 0; q1 < n; q1++) {
            for (int q2 = 0; q2 < n; q2++) {
                int q = FingerprintLTS.encode(q1, q2, n);
                for (int mu = 0; mu < S.getNumLabels(); mu++) {
                    int succ1 = S.transition(q1, mu);
                    int succ2 = S.transition(q2, mu);
                    if (succ1 >= 0 && succ2 >= 0) {
                        int succ = FingerprintLTS.encode(succ1, succ2, n);
                        int index = incoming_offset[succ] + incoming_edge_index[succ];
                        incoming_edge_q[index] = q;
                        incoming_edge_mu[index] = mu;
                        incoming_edge_index[succ]++;
                    }
                }
            }
        }

        for (int q = 0; q < nc; q++) {
            if (count[q] == 0) { // blocking → tester wins by θ
                W[wTail++] = q;
            }
        }

        while (wHead < wTail) {
            int p = W[wHead++];

            for (int i = incoming_offset[p]; i < incoming_offset[p + 1]; i++) {
                int q = incoming_edge_q[i];
                int mu = incoming_edge_mu[i];

                if (count[q] == 0)
                    continue; // already handled

                if (S.isInput(mu)) {
                    W[wTail++] = q;
                    count[q] = 0; // mark as invalid
                } else {
                    count[q]--;
                    if (count[q] == 0) {
                        W[wTail++] = q;
                    }
                }
            }
        }

        W = null;

        Result result = new Result(nc);
        for (int q = 0; q < nc; q++) {
            if (count[q] == 0) {
                result.isInvalid[q] = true;
            }
        }

        return result;
    }

    /**
     * Builds the compatibility relation for suspension automaton S using
     * q ♦ q' iff (q,q') is a VALID state of S||S.
     *
     * @param  A the LTS automaton wrapped in {@link FingerprintAutomaton}
     *
     * @return   the compatibility relation between states of S
     */
    public static FingerprintSplittingGraph.CompatibilityRelation computeCompatibility(
        FingerprintAutomaton A) {
        LOGGER.info("Compute Compatibility Relation");
        FingerprintLTS S = A.getCombined().automaton;
        Result res = computeNoCompose(S);
        int n2 = S.getNumStates();
        // (q1, q2) is compatible iff it is NOT invalid in S||S
        return (q1, q2) -> {
            int composedState = FingerprintLTS.encode(q1, q2, n2);
            return !res.isInvalid[composedState];
        };
    }

}
