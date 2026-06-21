package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class for the splitting graph used in fingerprinting
 */
public class FingerprintSplittingGraph {
    /** The suspension automaton this graph was built from */
    private final FingerprintLTS automaton;

    /** All vertices (non-empty subsets of states), stored canonically */
    private final Map<Set<Integer>, NodeData> nodes = new LinkedHashMap<>();

    /** Root = full state set */
    private final Set<Integer> root;

    /**
     * Create a new instance of a FingerprintSplittingGraph in respect to an LTS automaton
     *
     * @param automaton the automaton for which to create the Splitting Graph
     */
    public FingerprintSplittingGraph(FingerprintLTS automaton) {
        this.automaton = automaton;
        Set<Integer> all = new LinkedHashSet<>();
        for (int q = 0; q < automaton.getNumStates(); q++)
            all.add(q);
        this.root = canonical(all);
        nodes.put(root, new NodeData());
    }

    /**
     * Returns a canonical (unmodifiable, sorted) version of a set
     *
     * @param  set the set to be canonicalized
     *
     * @return     a canonical (unmodifiable, sorted) version of a set
     */
    public static Set<Integer> canonical(Set<Integer> set) {
        TreeSet<Integer> ts = new TreeSet<>(set);
        return Collections.unmodifiableSet(ts);
    }

    /**
     * Get the root of the splitting graph
     *
     * @return the root of the splitting graph
     */
    public Set<Integer> getRoot() {
        return root;
    }

    /**
     * Get the full set of nodes in the splitting graph
     *
     * @return the full set of nodes in the splitting graph
     */
    public Set<Set<Integer>> vertices() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    /**
     * Checks if a node is a leaf
     *
     * @param  v the node to check
     *
     * @return   true if it is a leaf, false otherwise
     */
    public boolean isLeaf(Set<Integer> v) {
        return nodes.containsKey(v) && nodes.get(v).children.isEmpty();
    }

    /**
     * Checks if a node is internal (not a leaf)
     *
     * @param  v the node to check
     *
     * @return   true is the node is not a leaf, false if it is
     */
    public boolean isInternal(Set<Integer> v) {
        return !isLeaf(v);
    }

    /**
     * Get the full set of children of a node in an unmodifiable Set
     *
     * @param  v the node
     *
     * @return   the unmodifiable set of its children
     */
    public Set<Set<Integer>> children(Set<Integer> v) {
        NodeData d = nodes.get(v);
        return d == null ? Collections.emptySet()
            : Collections.unmodifiableSet(d.children);
    }

    /**
     * Returns the witness CCSExpression of the node
     *
     * @param  v node
     *
     * @return   the witness of the node, or null if the node doesn't exist
     */
    public FingerprintCCSExpression witness(Set<Integer> v) {
        NodeData d = nodes.get(v);
        return d == null ? null : d.witness;
    }

    /**
     * Returns a list of all the leaves in the splitting graph
     *
     * @return a list of all the leaves in the splitting graph
     */
    public Set<Set<Integer>> leaves() {
        Set<Set<Integer>> result = new LinkedHashSet<>();
        for (Map.Entry<Set<Integer>, NodeData> e: nodes.entrySet()) {
            if (e.getValue().children.isEmpty())
                result.add(e.getKey());
        }
        return result;
    }

    /**
     * Returns a list of all the internal nodes in the splitting graph
     *
     * @return a list of all the internal nodes in the splitting graph
     */
    public Set<Set<Integer>> internalNodes() {
        Set<Set<Integer>> result = new LinkedHashSet<>();
        for (Map.Entry<Set<Integer>, NodeData> e: nodes.entrySet()) {
            if (!e.getValue().children.isEmpty())
                result.add(e.getKey());
        }
        return result;
    }

    /**
     * Converts a leaf into an internal node by splitting it: adds children and sets witness.
     *
     * @param  leaf                     the leaf to be converted into an internal node
     * @param  children                 the children to be added to the node
     * @param  witness                  the witness to be added to a leaf
     *
     * @throws IllegalArgumentException if the leaf to be split is not part of the splitting graph
     * @throws IllegalStateException    if the node to be split is not a leaf
     */
    public void split(Set<Integer> leaf, Set<Set<Integer>> children, FingerprintCCSExpression witness)
        throws IllegalArgumentException, IllegalStateException {
        if (!nodes.containsKey(leaf))
            throw new IllegalArgumentException("Node not in graph: " + leaf);
        if (!isLeaf(leaf))
            throw new IllegalStateException("Node is already internal: " + leaf);
        NodeData d = nodes.get(leaf);
        for (Set<Integer> child: children) {
            Set<Integer> can = canonical(child);
            nodes.putIfAbsent(can, new NodeData());
            d.children.add(can);
        }
        d.witness = witness;
    }

    /**
     * Finds the set of least common ancestors of P in this splitting graph.
     * An internal node v is an LCA of P if P ⊆ v and ∀c ∈ Post(v): P ⊄ c.
     * We do a top-down BFS from the root.
     *
     * @param  P the set of states to find the LCA of
     *
     * @return   the LCA of the set P
     */
    public Set<Set<Integer>> lca(Set<Integer> P) {
        Set<Set<Integer>> result = new LinkedHashSet<>();
        // BFS from root with visited set to prevent exponential re-visitation on DAGs
        Queue<Set<Integer>> queue = new ArrayDeque<>();
        Set<Set<Integer>> visited = new LinkedHashSet<>();
        queue.add(root);
        visited.add(root);
        while (!queue.isEmpty()) {
            Set<Integer> v = queue.poll();
            if (!v.containsAll(P))
                continue;

            boolean childContainsP = false;
            for (Set<Integer> c: children(v)) {
                if (c.containsAll(P)) {
                    childContainsP = true;
                    if (!visited.contains(c)) {
                        visited.add(c);
                        queue.add(c);
                    }
                }
            }
            if (!childContainsP && isInternal(v)) {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * Π(P, µ, v) = { (c before µ) ∩ P | c ∈ Post(v) } \ {∅}
     * Calculate the induced split of a node {@code P}, on symbol {@code μ}
     * based on the node {@code v}
     *
     * @param  P  the node to be split
     * @param  mu the label of the symbol μ
     * @param  v  the node v that guides the split
     *
     * @return    the set of children of P after the split
     */
    public Set<Set<Integer>> inducedSplit(Set<Integer> P, int mu, Set<Integer> v) {
        Set<Set<Integer>> result = new LinkedHashSet<>();
        for (Set<Integer> c: children(v)) {
            Set<Integer> cBeforeMu = automaton.before(c, mu);
            Set<Integer> inter = new LinkedHashSet<>(cBeforeMu);
            inter.retainAll(P);
            if (!inter.isEmpty()) {
                result.add(canonical(inter));
            }
        }
        return result;
    }

    /**
     * A splitting graph is complete if every leaf contains only pairwise compatible states.
     * This is checked externally using the compatibility relation.
     *
     * @param  compat the compatibility relation to be used
     *
     * @return        true if the graph is complete, false otherwise
     */
    public boolean isComplete(CompatibilityRelation compat) {
        for (Set<Integer> leaf: leaves()) {
            if (!compat.allCompatible(leaf))
                return false;
        }
        return true;
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    /**
     * Saves the data of a splitting graph node, aka children and witness CCSE
     */
    private static class NodeData {
        final Set<Set<Integer>> children = new LinkedHashSet<>();
        FingerprintCCSExpression witness = null;
    }

    /**
     * Interface for the compatibility relation of state pairs
     */
    public interface CompatibilityRelation {
        /**
         * Checks if two states are compatible
         *
         * @param  q1 the first state
         * @param  q2 the second state
         *
         * @return    true if they are compatible, false otherwise
         */
        boolean compatible(int q1, int q2);

        /**
         * Checks if all states in a set are pairwise compatible
         *
         * @param  states the set of states to be checked
         *
         * @return        true if all states are pairwise compatible, false otherwise
         */
        default boolean allCompatible(Set<Integer> states) {
            Integer[] arr = states.toArray(new Integer[0]);
            for (int i = 0; i < arr.length; i++)
                for (int j = i + 1; j < arr.length; j++)
                    if (!compatible(arr[i], arr[j]))
                        return false;
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SplittingGraph:\n");
        sb.append("  Root: ").append(root).append("\n");
        for (Map.Entry<Set<Integer>, NodeData> e: nodes.entrySet()) {
            Set<Integer> v = e.getKey();
            NodeData d = e.getValue();
            if (!d.children.isEmpty()) {
                sb.append("  ").append(v)
                    .append(" --[").append(d.witness).append("]--> ")
                    .append(d.children).append("\n");
            }
        }
        return sb.toString();
    }
}
