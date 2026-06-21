package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import java.util.*;

/**
 * CCS expression tree: F := 0 | F + F | µ.F
 * Used as witness functions and test case descriptors.
 * Expressions are immutable; structural sharing is used for efficiency.
 */
public abstract class FingerprintCCSExpression {

    /** Default constructor, should not be used */
    public FingerprintCCSExpression() {}

    /** The deadlock process 0 */
    public static final FingerprintCCSExpression ZERO = new Zero();

    /**
     * Creates a prefix CCSExpression (μ.F)
     *
     * @param  label the int label of the symbol of the prefix (μ)
     * @param  cont  the CCSExpression after the prefix (F)
     *
     * @return       a new instance of a Prefix expression label.cont
     */
    public static FingerprintCCSExpression prefix(int label, FingerprintCCSExpression cont) {
        return new Prefix(label, cont);
    }

    /**
     * Creates a choice CCSExpression (F + F')
     *
     * @param  left  the left CCSE (F)
     * @param  right the right CCSE (F')
     *
     * @return       a new instance of a Choice Expression (left + right) if both are different from ZERO,
     *                   or the non-ZERO expression, if one of them is ZERO
     */
    public static FingerprintCCSExpression choice(FingerprintCCSExpression left, FingerprintCCSExpression right) {
        if (left.equals(ZERO))
            return right;
        if (right.equals(ZERO))
            return left;
        return new Choice(left, right);
    }

    /**
     * Returns true if expression is ZERO
     *
     * @return true if expression is Zero, false otherwise
     */
    public abstract boolean isZero();

    /**
     * Returns true if expression is a Prefix
     *
     * @return true if expression is Prefix, false otherwise
     */
    public abstract boolean isPrefix();

    /**
     * Returns true if expression is a Choice
     *
     * @return true if expression is Choice, false otherwise
     */
    public abstract boolean isChoice();

    /**
     * Gets the int label of the prefix in a Prefix expression
     *
     * @return                               the int label of the prefix if the expression is a Prefix
     *
     * @throws UnsupportedOperationException if the expression is not a prefix
     */
    public int getLabel() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the continuation (F) in a Prefix expression (μ.F)
     *
     * @return                               the continuation if the expression is a Prefix
     *
     * @throws UnsupportedOperationException if the expression is not a prefix
     */
    public FingerprintCCSExpression getContinuation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the left part of a choice expression
     *
     * @return                               the left part if the expression is a Choice
     *
     * @throws UnsupportedOperationException if the expression is not a choice
     */
    public FingerprintCCSExpression getLeft() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the right part of a choice expression
     *
     * @return                               the right part if the expression is a Choice
     *
     * @throws UnsupportedOperationException if the expression is not a choice
     */
    public FingerprintCCSExpression getRight() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this deterministic CCS expression to its associated automaton.
     * States are subexpressions; initial state is this expression.
     *
     * @param  numInputs  the number of distinct input symbols
     * @param  numOutputs the number of distinct output symbols
     *
     * @return            a FingerprintLTS automaton that is equivalent to the CCSE
     */
    public FingerprintLTS toAutomaton(int numInputs, int numOutputs) {
        // Collect all subexpressions
        List<FingerprintCCSExpression> states = new ArrayList<>();
        IdentityHashMap<FingerprintCCSExpression, Integer> index = new IdentityHashMap<>();
        collectSubexpressions(this, states, index);

        int n = states.size();
        int L = numInputs + numOutputs;
        int[][] T = new int[n][L];
        for (int[] row: T)
            Arrays.fill(row, -1);

        // Build transitions from TCCS
        for (int i = 0; i < n; i++) {
            FingerprintCCSExpression e = states.get(i);
            buildTransitions(e, i, T, index);
        }
        return new FingerprintLTS(n, numInputs, numOutputs, T, index.get(this));
    }

    /**
     * Creates the set of subexpressions of a CCSE expression
     *
     * @param e     the initial expression
     * @param list  the list containing all subexpressions
     * @param index the output map of each suexpression to an index (useful for representing subexpressions as states)
     */
    private static void collectSubexpressions(FingerprintCCSExpression e,
        List<FingerprintCCSExpression> list,
        Map<FingerprintCCSExpression, Integer> index) {
        if (index.containsKey(e))
            return;
        index.put(e, list.size());
        list.add(e);
        if (e.isPrefix()) {
            collectSubexpressions(e.getContinuation(), list, index);
        } else if (e.isChoice()) {
            collectSubexpressions(e.getLeft(), list, index);
            collectSubexpressions(e.getRight(), list, index);
        }
    }

    /**
     * Builds transitions between CCSE expressions based on the following rules
     * TCCS rules:
     * (µ.F, µ, F) ∈ TCCS
     * If (F, µ, G) ∈ TCCS then (F+F', µ, G) ∈ TCCS
     * If (F, µ, G) ∈ TCCS then (F'+F, µ, G) ∈ TCCS
     *
     * @param e        the expression to derive transitions from
     * @param stateIdx the index of this expression in the subexpression to state map
     * @param T        the array to save transitions
     * @param index    the index map to find then index of accessed subexpressions as states
     */
    private static void buildTransitions(FingerprintCCSExpression e, int stateIdx,
        int[][] T, Map<FingerprintCCSExpression, Integer> index) {
        if (e.isPrefix()) {
            int mu = e.getLabel();
            int succ = index.get(e.getContinuation());
            T[stateIdx][mu] = succ;
        } else if (e.isChoice()) {
            // Propagate transitions from both branches
            propagate(e.getLeft(), stateIdx, T, index);
            propagate(e.getRight(), stateIdx, T, index);
        }
        // Zero: no transitions
    }

    /**
     * Recursive function to propagate the transitions in case of choice. Prefix is the base case based on the rules
     * TCCS rules:
     * (µ.F, µ, F) ∈ TCCS
     * If (F, µ, G) ∈ TCCS then (F+F', µ, G) ∈ TCCS
     * If (F, µ, G) ∈ TCCS then (F'+F, µ, G) ∈ TCCS
     *
     * @param e        the expression to derive transitions from
     * @param stateIdx the index of this expression in the subexpression to state map
     * @param T        the array to save transitions
     * @param index    the index map to find then index of accessed subexpressions as states
     */
    private static void propagate(FingerprintCCSExpression e, int stateIdx,
        int[][] T, Map<FingerprintCCSExpression, Integer> index) {
        if (e.isPrefix()) {
            int mu = e.getLabel();
            int succ = index.get(e.getContinuation());
            T[stateIdx][mu] = succ;
        } else if (e.isChoice()) {
            propagate(e.getLeft(), stateIdx, T, index);
            propagate(e.getRight(), stateIdx, T, index);
        }
    }

    /**
     * Computes Obs(AF): maximal traces reaching a leaf.
     *
     * @return returns as list of int-arrays for efficiency.
     */
    public List<int[]> observations() {
        List<int[]> result = new ArrayList<>();
        collectObs(this, new int[0], result);
        return result;
    }

    /**
     * Top-down collects the observations (maximal trace leading to a leaf) of a CCSE
     *
     * @param e      the CCSExpression to start from
     * @param prefix the trace of collected symbols before reaching this expression (Should be an empty array on first
     *                   call)
     * @param result save the trace once we reach a leaf
     */
    private static void collectObs(FingerprintCCSExpression e, int[] prefix, List<int[]> result) {
        if (e.isZero()) {
            result.add(prefix);
        } else if (e.isPrefix()) {
            int[] next = Arrays.copyOf(prefix, prefix.length + 1);
            next[prefix.length] = e.getLabel();
            collectObs(e.getContinuation(), next, result);
        } else { // choice
            collectObs(e.getLeft(), prefix, result);
            collectObs(e.getRight(), prefix, result);
        }
    }

    // ── Concrete subclasses ───────────────────────────────────────────────────

    /**
     * Defines the Zero expression. Defines custom equality and hashCode
     */
    private static final class Zero extends FingerprintCCSExpression {
        @Override
        public boolean isZero() {
            return true;
        }

        @Override
        public boolean isPrefix() {
            return false;
        }

        @Override
        public boolean isChoice() {
            return false;
        }

        @Override
        public String toString() {
            return "0";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Zero;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    /**
     * Defines the Prefix expression. Defines custom equality and hashCode
     */
    private static final class Prefix extends FingerprintCCSExpression {
        private final int label;
        private final FingerprintCCSExpression cont;

        Prefix(int label, FingerprintCCSExpression cont) {
            this.label = label;
            this.cont = cont;
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public boolean isPrefix() {
            return true;
        }

        @Override
        public boolean isChoice() {
            return false;
        }

        @Override
        public int getLabel() {
            return label;
        }

        @Override
        public FingerprintCCSExpression getContinuation() {
            return cont;
        }

        @Override
        public String toString() {
            return label + ".(" + cont + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Prefix p))
                return false;
            return label == p.label && cont.equals(p.cont);
        }

        @Override
        public int hashCode() {
            return 31 * label + cont.hashCode();
        }
    }

    /**
     * Defines the Choice expression. Defines custom equality and hashCode
     */
    private static final class Choice extends FingerprintCCSExpression {
        private final FingerprintCCSExpression left, right;

        Choice(FingerprintCCSExpression left, FingerprintCCSExpression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public boolean isPrefix() {
            return false;
        }

        @Override
        public boolean isChoice() {
            return true;
        }

        @Override
        public FingerprintCCSExpression getLeft() {
            return left;
        }

        @Override
        public FingerprintCCSExpression getRight() {
            return right;
        }

        @Override
        public String toString() {
            return "(" + left + " + " + right + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Choice c))
                return false;
            return left.equals(c.left) && right.equals(c.right);
        }

        @Override
        public int hashCode() {
            return left.hashCode() * 31 + right.hashCode();
        }
    }
}
