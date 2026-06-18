package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintAutomaton;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintCCSExpression;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintLTS;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintSplittingGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creates the Splitting graph of the LTS automaton
 */
public final class FingerprintSplittingGraphExtraction {
    private static final Logger LOGGER = LogManager.getLogger();
    private final FingerprintAutomaton A;
    private final FingerprintSplittingGraph.CompatibilityRelation compat;

    /**
     * Constructor for the class that calculates the splitting graph
     *
     * @param  A                        the Automaton wrapped in {@link FingerprintAutomaton} for which the Splitting
     *                                      Graph will be calculated
     *
     * @throws IllegalArgumentException if it is not a suspension automaton
     */
    public FingerprintSplittingGraphExtraction(FingerprintAutomaton A) throws IllegalArgumentException {
        if (!A.getCombined().automaton.isSuspensionAutomaton()) {
            this.A = null;
            this.compat = null;
            throw new IllegalArgumentException("Input must have a suspension automaton.");
        }
        this.A = A;
        if (A.getCombined() != null) {
            this.compat = A.getCompat();
        } else {
            this.compat = FingerprintComputeCompatibility.computeCompatibility(A);
        }
    }

    /**
     * Constructor for the class that calculates the splitting graph
     *
     * @param  A                        the Automaton wrapped in {@link FingerprintAutomaton} for which the Splitting
     *                                      Graph will be
     *                                      calculated
     * @param  compat                   the compatibility relation to be used to determine if states can be
     *                                      distinguished
     *
     * @throws IllegalArgumentException if it is not a suspension automaton
     */
    public FingerprintSplittingGraphExtraction(FingerprintAutomaton A,
        FingerprintSplittingGraph.CompatibilityRelation compat) throws IllegalArgumentException {
        if (!A.getCombined().automaton.isSuspensionAutomaton()) {
            this.A = null;
            this.compat = null;
            throw new IllegalArgumentException("Input must have a suspension automaton.");
        }
        this.A = A;
        this.compat = compat;
    }

    /**
     * Constructs a complete splitting graph for S.
     * Uses a FIFO worklist of leaves containing incompatible pairs.
     * If a leaf cannot be split yet (waiting for an LCA to be created by
     * another split), it is deferred to the back of the queue.
     * The stall counter caps re-queuing: if nothing in the worklist can be
     * split, the remaining leaves are genuinely unsplittable.
     *
     * @return the splittingGraph
     */
    public FingerprintSplittingGraph build() {
        LOGGER.info("Compute Splitting Graph");
        FingerprintSplittingGraph Y = new FingerprintSplittingGraph(A.getCombined().automaton);

        // Worklist of leaves containing incompatible pairs.
        // We process them in rounds. In each round we attempt to split every
        // pending leaf. If at least one split succeeds in a round we start a
        // fresh round (new splits may have created LCAs for deferred leaves).
        // We stop when a full round produces zero splits (genuine deadlock —
        // occurs only when compatible state pairs prevent a complete splitting).
        List<Set<Integer>> pending = new ArrayList<>();
        if (!compat.allCompatible(Y.getRoot()))
            pending.add(Y.getRoot());

        while (true) {
            List<Set<Integer>> nextRound = new ArrayList<>();
            boolean anyProgress = false;

            for (Set<Integer> leaf: pending) {
                if (!Y.isLeaf(leaf) || compat.allCompatible(leaf))
                    continue;

                boolean split = trySplit(Y, leaf);
                if (split) {
                    anyProgress = true;
                    for (Set<Integer> child: Y.children(leaf)) {
                        if (!compat.allCompatible(child))
                            nextRound.add(child);
                    }
                } else {
                    nextRound.add(leaf); // defer to next round
                }
            }

            if (!anyProgress)
                break; // fixed point — no further progress possible
            pending = nextRound;
        }

        return Y;
    }

    /**
     * Attempts to split leaf l in splitting graph Y. Prefers output split over input split.
     *
     * @param  Y the splitting graph
     * @param  l leaf to be split
     *
     * @return   Returns true if a split was performed, false if not yet possible.
     */
    private boolean trySplit(FingerprintSplittingGraph Y, Set<Integer> l) {
        if (tryOutputSplit(Y, l))
            return true;
        if (tryInputSplit(Y, l))
            return true;
        return false;
    }

    /**
     * Attempts to split leaf l on output in splitting graph Y. Prefers output split over input split.
     *
     * @param  Y the splitting graph
     * @param  l leaf to be split
     *
     * @return   Returns true if a split was performed, false if not yet possible.
     */
    private boolean tryOutputSplit(FingerprintSplittingGraph Y, Set<Integer> l) {
        FingerprintLTS S = A.getCombined().automaton;
        Set<Set<Integer>> C = new LinkedHashSet<>();
        FingerprintCCSExpression F = FingerprintCCSExpression.ZERO;

        for (int x: S.out(l)) {
            Set<Integer> enabledLX = S.enabled(l, x);

            if (enabledLX.size() < l.size()) {
                // ∃q∈l: x∉out(q) → direct strict-subset split (lines 7-9)
                C.add(FingerprintSplittingGraph.canonical(enabledLX));
                F = FingerprintCCSExpression.choice(F,
                    FingerprintCCSExpression.prefix(x, FingerprintCCSExpression.ZERO));

            } else {
                // enabled(l,x) = l → induced split via LCA (lines 11-13)
                Set<Integer> lAfterX = S.after(l, x);
                Set<Set<Integer>> lcaSet = Y.lca(lAfterX);
                if (!lcaSet.isEmpty()) {
                    Set<Integer> v = lcaSet.iterator().next();
                    Set<Set<Integer>> pi = Y.inducedSplit(l, x, v);
                    boolean anyAdded = false;
                    for (Set<Integer> d: pi) {
                        if (!d.isEmpty() && d.size() < l.size()) {
                            C.add(d);
                            anyAdded = true;
                        }
                    }
                    if (anyAdded) {
                        FingerprintCCSExpression wv = Y.witness(v);
                        F = FingerprintCCSExpression.choice(F,
                            FingerprintCCSExpression.prefix(x, wv != null ? wv : FingerprintCCSExpression.ZERO));
                    }
                }
                // else: no LCA yet — skip this x
            }
        }

        if (C.isEmpty())
            return false;
        Y.split(l, ensureCoverage(l, C), F);
        return true;
    }

    /**
     * Attempts to split leaf l on input in splitting graph Y. Prefers output split over input split.
     *
     * @param  Y the splitting graph
     * @param  l leaf to be split
     *
     * @return   Returns true if a split was performed, false if not yet possible. *
     */

    private boolean tryInputSplit(FingerprintSplittingGraph Y, Set<Integer> l) {
        FingerprintLTS S = A.getCombined().automaton;
        for (int a: S.in(l)) {
            Set<Integer> lAfterA = S.after(l, a);
            Set<Set<Integer>> lcaSet = Y.lca(lAfterA);
            if (lcaSet.isEmpty())
                continue;

            Set<Integer> v = lcaSet.iterator().next();
            Set<Set<Integer>> pi = Y.inducedSplit(l, a, v);

            // Line 19: add l \ enabled(l, a) to each element of Π
            Set<Integer> notEnabledA = new LinkedHashSet<>(l);
            notEnabledA.removeAll(S.enabled(l, a));

            Set<Set<Integer>> C = new LinkedHashSet<>();
            for (Set<Integer> d: pi) {
                TreeSet<Integer> child = new TreeSet<>(d);
                child.addAll(notEnabledA);
                if (!child.isEmpty() && child.size() < l.size()) {
                    C.add(FingerprintSplittingGraph.canonical(child));
                }
            }
            if (C.isEmpty() && !notEnabledA.isEmpty() && notEnabledA.size() < l.size()) {
                C.add(FingerprintSplittingGraph.canonical(notEnabledA));
            }

            if (C.isEmpty())
                continue;

            FingerprintCCSExpression wv = Y.witness(v);
            FingerprintCCSExpression F = FingerprintCCSExpression.prefix(a,
                wv != null ? wv : FingerprintCCSExpression.ZERO);
            Y.split(l, ensureCoverage(l, C), F);
            return true;
        }
        return false;
    }

    /**
     * Ensures every state of l appears in ⋃C, where C is the set of children in
     * the splitting graph.
     * Any uncovered states are merged into all the children.
     *
     * @param  l the node l to check coverage
     * @param  C the set of children of l
     *
     * @return   the new set of children with the added states
     */
    private Set<Set<Integer>> ensureCoverage(Set<Integer> l, Set<Set<Integer>> C) {
        Set<Integer> covered = new LinkedHashSet<>();
        for (Set<Integer> child: C)
            covered.addAll(child);
        Set<Integer> uncovered = new LinkedHashSet<>(l);
        uncovered.removeAll(covered);
        if (uncovered.isEmpty())
            return C;

        Set<Set<Integer>> result = new LinkedHashSet<>();
        for (Set<Integer> child: C) {
            TreeSet<Integer> extended = new TreeSet<>(child);
            extended.addAll(uncovered);
            result.add(FingerprintSplittingGraph.canonical(extended));
        }

        if (result.isEmpty()) {
            result.add(FingerprintSplittingGraph.canonical(new TreeSet<>(uncovered)));
        }

        return result;
    }

}
