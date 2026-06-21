package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintGenerateLTS;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/** Class for the nodes of the Adaptive Distinguish Graph tree */
public class FingerprintNode {
    /** Label of incoming edge (null if root */
    String edgeLabel;
    /** Map of children with their edge labels */
    final LinkedHashMap<String, FingerprintNode> children = new LinkedHashMap<>();
    /** States identified at the node, non-null only at leaves */
    Set<Integer> states;
    /** Model names identified at the node, non-null only at leaves */
    Set<String> models;

    /**
     * Creates a new instance of a {@link FingerprintNode} with the given edgeLabel
     *
     * @param edgeLabel the edge label for the node
     */
    public FingerprintNode(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }

    /**
     * Updates the state set of the node. Should be only used for leaves
     *
     * @param  states                the states to add to the node
     *
     * @throws IllegalStateException if node is not a leaf
     */
    public void updateStates(Set<Integer> states) throws IllegalStateException {
        if (!this.isLeaf())
            throw new IllegalStateException("Cannot set states on non-leaf node");
        this.states = states;
    }

    /**
     * Get states of the node. Should be only used for leaves
     *
     * @return                       the set of states identified in the node
     *
     * @throws IllegalStateException if node is not a leaf
     */
    public Set<Integer> getStates() throws IllegalStateException {
        if (!this.isLeaf())
            throw new IllegalStateException("Cannot get states of non-leaf node");
        return states;
    }

    /**
     * Add a child to the node
     *
     * @param  child                 the node to be added as child
     *
     * @throws IllegalStateException if child is added to a node with non-null states
     * @throws RuntimeException      if another child with the same label exists
     */
    public void addChild(FingerprintNode child) throws IllegalStateException, RuntimeException {
        if (states != null)
            throw new IllegalStateException("Cannot add child to leaf node");
        if (child == null) {
            // System.err.println("Warning: Attempted to add null child with edge label '" + edgeLabel + "' to node with
            // edge label '" + this.edgeLabel + "'. This child will be ignored.");
            return; // Do not add null children
        }
        String edgeLabel = child.getEdgeLabel();
        if (children.get(edgeLabel) != null)
            throw new RuntimeException("Child with label " + edgeLabel + "already exists");
        children.put(edgeLabel, child);
    }

    /**
     * Get the map of edgeLabels to children nodes
     *
     * @return the children of the node with their edgeLabels
     */
    public java.util.Map<String, FingerprintNode> getChildren() {
        return children;
    }

    /**
     * Empty the children of a node
     */
    public void emptyChildren() {
        children.clear();
    }

    /**
     * Remove the specified child
     *
     * @param edgeLabel the edge label of the child to remove
     */
    public void removeChild(String edgeLabel) {
        children.remove(edgeLabel);
    }

    /**
     * Update the edge label of a node (that is not the root)
     *
     * @param  newLabel              the new edge label
     *
     * @throws IllegalStateException if the node is root (null edgeLabel)
     */
    public void updateEdgeLabel(String newLabel) throws IllegalStateException {
        if (edgeLabel == null)
            throw new IllegalStateException("Cannot update edge label of root node");
        this.edgeLabel = newLabel;
    }

    /**
     * Getter for the edge label
     *
     * @return the edgeLabel of the node
     */
    public String getEdgeLabel() {
        return edgeLabel;
    }

    /**
     * Transforms states to the names of the models they belong to
     *
     * @param combined the combined LTS with metadata for State to Model match
     */
    public void modelsFromStates(FingerprintGenerateLTS.CombinedLTS combined) {
        models = new LinkedHashSet<>();
        if (isLeaf()) {
            models.addAll(combined.modelsIn(this.states));
        } else {
            throw new IllegalStateException("Cannot set models on non-leaf node");
        }
    }

    /**
     * Getter for the set of models in a leaf
     *
     * @return                       the set of models
     *
     * @throws IllegalStateException if the node is not a leaf
     */
    public Set<String> getModels() throws IllegalStateException {
        if (models == null)
            throw new IllegalStateException("Cannot get models of non-leaf node");
        return models;
    }

    /**
     * Update the set of models
     *
     * @param  models                the new set of models
     *
     * @throws IllegalStateException if the node is not a leaf
     */
    public void updateModels(Set<String> models) throws IllegalStateException {
        if (!isLeaf())
            throw new IllegalStateException("Cannot set models on non-leaf node");
        this.models = models;
    }

    /**
     * Checks if the node is a leaf (has no children)
     *
     * @return true if the node is a leaf, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns all the states in the subtree rooted at the node
     *
     * @return all the states in the subtree rooted at the node
     */
    Set<Integer> allStates() {
        if (isLeaf())
            return states != null ? states : Collections.emptySet();
        Set<Integer> r = new LinkedHashSet<>();
        for (FingerprintNode c: children.values())
            r.addAll(c.allStates());
        return r;
    }
}
