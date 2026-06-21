package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintAutomaton;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintCCSExpression;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintLTS;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintSplittingGraph;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintGenerateLTS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains the algorithm to create an implementation decision tree given a wrapped combined LTS
 * automaton {@link FingerprintGenerateLTS.CombinedLTS}
 */
public class FingerprintExtractDecisionTree {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The combined LTS */
    private FingerprintGenerateLTS.CombinedLTS combined;
    /** The automaton of the LTS */
    private FingerprintLTS S;
    /** The Splitting Graph of the LTS */
    private FingerprintSplittingGraph Y;
    /** The compatibility relation of the LTS */
    private FingerprintSplittingGraph.CompatibilityRelation compat;
    /** The converter used to turn the automaton back to models */
    private FingerprintGenerateLTS converter;

    private int resLabel = -1;
    private int emptyLabel = -1;

    /**
     * The constructor of the FingerprintExtractDecisionTree class
     *
     * @param A the automaton for which the Decision Tree is made
     */
    public FingerprintExtractDecisionTree(FingerprintAutomaton A) throws IllegalArgumentException {
        this.combined = A.getCombined();
        if (A.getCompat() != null) {
            this.compat = A.getCompat();
        } else {
            this.compat = FingerprintComputeCompatibility.computeCompatibility(A);
        }
        if (A.getSplittingGraph() != null) {
            this.Y = A.getSplittingGraph();
        } else {
            try {
                this.Y = new FingerprintSplittingGraphExtraction(A, this.compat).build();
            }
            catch (IllegalArgumentException e) {
                LOGGER.error("Error while getting Splitting Graph");
                this.Y = null;
            }
        }
        this.converter = A.getConverter();

        this.S = combined.automaton;
        this.resLabel = converter.labelIndex("reset");
        this.emptyLabel = converter.labelIndex("");
    }

    /**
     * Computes the Adaptive Distinguish Graph of the models saved in the LTS
     *
     * @return the root of the Adaptive Distinguish Graph
     */
    public FingerprintNode compute() throws IllegalStateException {
        LOGGER.info("Compute Adaptive Distinguish Graph - ADG");
        Set<Integer> initialStates = combined.initialStates();
        LOGGER.debug("Initial states: " + initialStates);
        FingerprintNode root = null;
        try {
            root = compDG(initialStates);
        }
        catch (IllegalStateException e) {
            throw e;
        }
        LOGGER.info("Initial compDG finished, now annotating with models...");
        annotateWithModels(root, combined);
        LOGGER.info("Annotation finished, now collapsing uniform branches...");
        collapseUniformBranches(root);
        LOGGER.info("Collapsing finished, now condensing...");
        condense(root);
        return root;
    }

    /**
     * Computes the ADG top-down
     *
     * @param  P Set of initial states of models to distinguish between
     *
     * @return   the root of the ADG
     */

    private FingerprintNode compDG(Set<Integer> P) throws IllegalStateException {
        try {
            return compDG(P, Collections.emptySet(), FingerprintCCSExpression.ZERO, null);
        }
        catch (IllegalStateException e) {
            throw e;
        }
    }

    /**
     * Recursive function to compute the ADG top-down
     *
     * @param  P           current set of automaton states reachable at this point
     * @param  stuckStates states that did not accept a previous input and are thus carried to this call separately,
     *                         should be null in the initial call
     * @param  F           current CCS expression being built
     * @param  label       the label of the root of the current subtree, should be null in the initial call
     *
     * @return             the root of the subtree of the ADG
     */
    private FingerprintNode compDG(Set<Integer> P, Set<Integer> stuckStates, FingerprintCCSExpression F, String label)
        throws IllegalStateException {

        FingerprintNode result;

        if (P.isEmpty())
            return null; // No states to distinguish, return null (no node)

        // Base case: all current states ara compatible
        if (compat.allCompatible(P)) {
            // If stuck states is empty then we are at a leaf
            if (stuckStates.isEmpty() && combined.modelsIn(P).size() < 2) {
                result = new FingerprintNode(label);
                result.updateStates(P);
                return result;
            }

            if (stuckStates.isEmpty() && compat.allCompatible(combined.initialStates(P))) {
                // We are at a leaf with multiple models; return a leaf with all those models
                result = new FingerprintNode(label);
                result.updateStates(P);
                return result;
            }

            // Otherwise, we are at a leaf but with some stuck states; return F
            result = new FingerprintNode(label);
            FingerprintNode resNode = new FingerprintNode("reset");
            result.addChild(resNode);
            Set<Integer> newP = S.after(S.after(P, resLabel), emptyLabel); // For input states in P
            newP.addAll(S.after(P, emptyLabel)); // For output states in P
            newP.addAll(S.after(S.after(stuckStates, resLabel), emptyLabel)); // For stuck states (they can only be
                                                                              // input states)
            FingerprintNode child = compDG(newP, Collections.emptySet(), FingerprintCCSExpression.ZERO, "");
            resNode.addChild(child);

            return result;
        }

        if (F.isZero()) {
            Set<Integer> Pcan = FingerprintSplittingGraph.canonical(P);
            // Choose best (most injective) LCA
            Set<Set<Integer>> lcas = Y.lca(Pcan);
            if (lcas.isEmpty()) {
                // No LCA — Something went wrong, it is here just for completeness
                return null;
            } else {
                Set<Integer> bestLCA = chooseBestLCA(lcas, P);
                FingerprintCCSExpression witness = Y.witness(bestLCA);
                result = compDG(P, stuckStates, witness, label);
            }
        } else if (F.isPrefix()) {
            int mu = F.getLabel();
            Set<Integer> Pmu = S.after(P, mu);
            Set<Integer> newStuck = new LinkedHashSet<>(stuckStates);
            if (S.isInput(mu)) {
                for (int q: P) {
                    if (S.transition(q, mu) < 0 && combined.isOriginalState(q))
                        newStuck.add(q);
                    // System.err.println("For input " + converter.labelName(mu) + " model " + combined.modelOf(q) + "
                    // goes to stuck");
                }
            }
            FingerprintNode extended = compDG(Pmu, newStuck, F.getContinuation(), converter.labelName(mu));

            if (S.isInput(mu) && !newStuck.isEmpty()) {

                FingerprintNode otherNode = new FingerprintNode("other");
                FingerprintNode resNode = new FingerprintNode("reset");
                otherNode.addChild(resNode);
                Set<Integer> newP = S.after(S.after(newStuck, resLabel), emptyLabel); // For stuck states (they can only
                                                                                      // be input states)
                // System.err.println("At leaf with stuck states " + stuckStates + ", newP after reset and empty is " +
                // newP);
                FingerprintNode child = compDG(newP, Collections.emptySet(), FingerprintCCSExpression.ZERO, "");
                resNode.addChild(child);

                if (extended != null) {
                    extended.addChild(otherNode);
                } else {
                    extended = new FingerprintNode(converter.labelName(mu));
                    extended.addChild(otherNode);
                }
            }

            result = new FingerprintNode(label);
            result.addChild(extended);
        } else if (F.isChoice()) {
            List<FingerprintCCSExpression> branches = new ArrayList<>();
            flattenChoice(F, branches);

            result = new FingerprintNode(label);
            for (FingerprintCCSExpression branch: branches) {
                if (branch.isZero()) {
                    continue;
                }
                if (!branch.isPrefix()) {
                    throw new IllegalStateException("Expected only prefix branches after flattening");
                }
                int mu = branch.getLabel();
                Set<Integer> Pmu = S.after(P, mu);
                FingerprintNode extended = compDG(Pmu, stuckStates, branch.getContinuation(), converter.labelName(mu));
                result.addChild(extended);
            }

        } else {
            throw new IllegalStateException("Unknown CCS expression type");
        }

        return result;
    }

    /**
     * Among candidate LCAs, choose the one whose first split label is
     * injective for the most incompatible pairs in P (Definition 33).
     * Injectivity: µ is injective for P if for all q≠q' ∈ P with q≬q':
     * T(q,µ)↓ ∧ T(q',µ)↓ ∧ T(q,µ) ≬ T(q',µ)
     * OR µ ∈ O \ (out(q) ∩ out(q'))
     */
    private Set<Integer> chooseBestLCA(Set<Set<Integer>> candidates, Set<Integer> P) {
        if (candidates.size() == 1)
            return candidates.iterator().next();
        Set<Integer> best = null;
        int bestScore = -1;

        for (Set<Integer> v: candidates) {
            int score = injectivityScore(v, P);
            if (score > bestScore) {
                bestScore = score;
                best = v;
            }
        }
        return best != null ? best : candidates.iterator().next();
    }

    /**
     * Score = number of incompatible pairs (q,q') in P for which the first
     * action of witness(v) is injective.
     */
    private int injectivityScore(Set<Integer> v, Set<Integer> P) {
        FingerprintCCSExpression w = Y.witness(v);
        if (w == null || w.isZero())
            return 0;

        // Collect the first labels of the witness
        Set<Integer> firstLabels = firstLabels(w);
        int score = 0;
        Integer[] arr = P.toArray(new Integer[0]);
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                int q1 = arr[i], q2 = arr[j];
                if (!compat.compatible(q1, q2)) {
                    for (int mu: firstLabels) {
                        if (isInjective(mu, q1, q2)) {
                            score++;
                            break;
                        }
                    }
                }
            }
        }
        return score;
    }

    private Set<Integer> firstLabels(FingerprintCCSExpression f) {
        Set<Integer> result = new LinkedHashSet<>();
        collectFirstLabels(f, result);
        return result;
    }

    private void collectFirstLabels(FingerprintCCSExpression f, Set<Integer> out) {
        if (f.isPrefix()) {
            out.add(f.getLabel());
        } else if (f.isChoice()) {
            collectFirstLabels(f.getLeft(), out);
            collectFirstLabels(f.getRight(), out);
        }
    }

    /**
     * Calculate whether transition mu is injective for the
     * pair (q1, q2).
     * <p>
     * µ is injective for the pair (q, q') with q ≬ q' if:
     * [T(q,µ)↓ ∧ T(q',µ)↓ ∧ T(q,µ) ≬ T(q',µ)]
     * OR [µ ∈ O \ (out(q) ∩ out(q'))]
     *
     * @param  mu the label of the transition
     * @param  q1 the label of the first state
     * @param  q2 the label of the second state
     *
     * @return    true if mu is injective for (q1, q2), false otherwise *
     */
    private boolean isInjective(int mu, int q1, int q2) {
        if (S.isOutput(mu)) {
            // µ ∈ O \ (out(q) ∩ out(q')) OR both go to incompatible successors
            boolean inBoth = S.out(q1).contains(mu) && S.out(q2).contains(mu);
            if (!inBoth)
                return true; // distinguishes by enabling
            int s1 = S.transition(q1, mu);
            int s2 = S.transition(q2, mu);
            return s1 >= 0 && s2 >= 0 && !compat.compatible(s1, s2);
        } else {
            // Input: both must enable it and successors must be incompatible
            int s1 = S.transition(q1, mu);
            int s2 = S.transition(q2, mu);
            return s1 >= 0 && s2 >= 0 && !compat.compatible(s1, s2);
        }
    }

    /**
     * Flattens multiple nested binary choice expressions into one with multiple branches
     *
     * @param e   the initial choice expression
     * @param out the output list of branches
     */
    private static void flattenChoice(FingerprintCCSExpression e, List<FingerprintCCSExpression> out) {
        if (e.isChoice()) {
            flattenChoice(e.getLeft(), out);
            flattenChoice(e.getRight(), out);
        } else
            out.add(e);
    }

    /**
     * Annotates leaves with the respective models of the states in them
     *
     * @param root     the root of the non-annotated ADG
     * @param combined the combined LTS automaton to be used for state to model conversion
     */
    private static void annotateWithModels(FingerprintNode root, FingerprintGenerateLTS.CombinedLTS combined) {
        if (root == null)
            return;
        if (root.isLeaf()) {
            root.modelsFromStates(combined);
        } else {
            for (FingerprintNode c: root.getChildren().values())
                annotateWithModels(c, combined);
        }
    }

    /**
     * Collapses any subtree whose leaves all carry the same model set into a
     * single leaf with that model set.
     * <p>
     * Runs bottom-up to fixpoint so that collapsing a subtree can expose
     * a newly-uniform parent.
     *
     * @param root the root of the ADG
     */
    private static void collapseUniformBranches(FingerprintNode root) {
        if (root == null)
            return;
        boolean changed = true;
        while (changed)
            changed = collapsePass(root);
    }

    /**
     * One bottom-up pass. Returns true if any node was collapsed.
     *
     * @param  node the root of the subtree check
     *
     * @return      true is there was any collapse, false otherwise
     */
    private static boolean collapsePass(FingerprintNode node) {
        if (node.isLeaf())
            return false;
        boolean changed = false;

        // Recurse first (bottom-up)
        for (FingerprintNode c: new ArrayList<>(node.getChildren().values()))
            changed |= collapsePass(c);

        // Check each child subtree: if all its leaves share the same model set,
        // collapse it to a single leaf.

        if (node.getChildren().size() < 2)
            return false; // No children to collapse

        for (String key: new ArrayList<>(node.getChildren().keySet())) {
            FingerprintNode child = node.getChildren().get(key);
            if (child == null)
                throw new IllegalStateException("Unexpected null child in key " + key);
            if (child.isLeaf())
                continue; // already a leaf, nothing to collapse

            Set<String> uniform = uniformLeafModels(child);
            if (uniform != null) {
                // Replace the entire child subtree with a single leaf
                FingerprintNode leaf = new FingerprintNode(key);
                leaf.updateModels(uniform);
                child.emptyChildren();
                child.addChild(leaf);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * If every leaf in the subtree rooted at {@code node} has exactly the same
     * model set, returns that set.
     *
     * @param  node the root of the subtree to examine
     *
     * @return      if all the leaves of the subtree have the same set then returns the set. Null otherwise
     */
    private static Set<String> uniformLeafModels(FingerprintNode node) {
        Set<String> reference = null;
        for (FingerprintNode leaf: collectLeaves(node)) {
            Set<String> m = leaf.getModels();
            if (reference == null) {
                reference = m;
            } else if (!reference.equals(m)) {
                return null; // leaves differ — not uniform
            }
        }
        return reference; // null only if there were no leaves (shouldn't happen)
    }

    /**
     * Collect all leaf nodes in a subtree.
     *
     * @param  node the root of the subtree
     *
     * @return      a list of all the leaves
     */
    private static List<FingerprintNode> collectLeaves(FingerprintNode node) {
        List<FingerprintNode> leaves = new ArrayList<>();
        collectLeavesInto(node, leaves);
        return leaves;
    }

    /**
     * Collect all the leaves of a subtree to an output list
     *
     * @param node the root of the subtree
     * @param out  the list to save the leaves
     */
    private static void collectLeavesInto(FingerprintNode node, List<FingerprintNode> out) {
        if (node.isLeaf()) {
            out.add(node);
            return;
        }
        for (FingerprintNode c: node.getChildren().values())
            collectLeavesInto(c, out);
    }

    /**
     * Condense paths that only have one leaf
     *
     * @param root the root of the ADG
     */
    private static void condense(FingerprintNode root) {
        // Only meaningful when there are multiple models to distinguish
        // if (allModels.size() <= 1) return;
        boolean changed = true;
        while (changed)
            changed = condensePass(root);
    }

    /**
     * Condense paths that only have one leaf in the subtree
     *
     * @param  node the root of the subtree
     *
     * @return      true if a path has been condensed, false otherwise
     */
    private static boolean condensePass(FingerprintNode node) {
        if (node.isLeaf())
            return false;
        boolean changed = false;
        for (FingerprintNode c: new ArrayList<>(node.getChildren().values()))
            changed |= condensePass(c);

        // // Remove leaves covering all models (no distinguishing power left)
        // for (String key : new ArrayList<>(node.getChildren().keySet())) {
        // FingerprintNode child = node.getChildren().get(key);
        // if (child.isLeaf() && child.getModels().equals(allModels)) {
        // node.removeChild(key);
        // changed = true;
        // }
        // }

        // Collapse input nodes with a single output child that is a leaf
        for (String key: new ArrayList<>(node.getChildren().keySet())) {
            FingerprintNode child = node.getChildren().get(key);
            if (child.isLeaf() || child.getChildren().size() != 1)
                continue;
            FingerprintNode grandchild = child.getChildren().values().iterator().next();
            if (grandchild.isLeaf()) {
                child.emptyChildren();
                child.updateModels(new LinkedHashSet<>(grandchild.getModels()));
                changed = true;
            }
        }
        return changed;
    }
}
