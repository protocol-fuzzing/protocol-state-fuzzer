package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * A simple adaptive discriminating graph used for model identification.
 * <p>
 * The graph consists of {@link Node} instances where each non-leaf node
 * represents a decision point for input selection and each leaf node holds
 * a set of candidate models.
 */
public class IdentifierAdg {

    /**
     * A node inside the ADG.
     */
    public static class Node {

        String edgeLabel; // label on incoming edge (null for root)
        final LinkedHashMap<String, Node> children = new LinkedHashMap<>();
        Set<String> models; // non-null at leaf nodes

        /**
         * Creates a node with the given incoming edge label.
         *
         * @param edgeLabel the label on the incoming edge, or {@code null} for the root
         */
        public Node(String edgeLabel) {
            this.edgeLabel = edgeLabel;
        }

        /**
         * Adds a child node for the given edge label.
         *
         * @param  edgeLabel             the label representing the edge to the child node
         * @param  child                 the child node to add
         *
         * @throws IllegalStateException if this node is already a leaf
         */
        public void addChild(String edgeLabel, Node child) {
            if (models != null)
                throw new IllegalStateException("Cannot add child to leaf node");
            if (child == null) {
                return;
            }
            children.put(edgeLabel, child);
        }

        /**
         * Returns the number of children of this node.
         *
         * @return number of child nodes
         */
        public Integer countChildren() {
            return this.children.size();
        }

        /**
         * Returns the child nodes mapped by outgoing edge labels.
         *
         * @return map of edge labels to child nodes
         */
        public java.util.Map<String, Node> getChildren() {
            return children;
        }

        /**
         * Removes all children from this node.
         */
        public void emptyChildren() {
            children.clear();
        }

        /**
         * Returns the incoming edge label for this node.
         *
         * @return the edge label or {@code null} for the root node
         */
        public String getEdgeLabel() {
            return edgeLabel;
        }

        /**
         * Sets a new incoming edge label for this node.
         *
         * @param newLabel the new label to set
         */
        public void updateEdgeLabel(String newLabel) {
            edgeLabel = newLabel;
        }

        /**
         * Returns the candidate models stored in this leaf node.
         *
         * @return                       the model set
         *
         * @throws IllegalStateException if the node is not a leaf
         */
        public Set<String> getModels() {
            if (models == null)
                return Collections.emptySet();
            return models;
        }

        /**
         * Updates the set of candidate models on this leaf node.
         *
         * @param  models                the set of models supported by this leaf
         *
         * @throws IllegalStateException if the node is not a leaf
         */
        public void updateModels(Set<String> models) {
            if (!isLeaf())
                throw new IllegalStateException("Cannot set models on non-leaf node");
            this.models = models;
        }

        /**
         * Returns whether this node is a leaf node.
         *
         * @return {@code true} if the node has no children, otherwise {@code false}
         */
        public boolean isLeaf() {
            return children.isEmpty();
        }

    }

    /** Root node of the ADG. */
    protected Node root;

    /** Current node used during identification. */
    protected Node currentNode;

    /**
     * Creates the identifier ADG with the given root node.
     *
     * @param root the root of the ADG
     */
    public IdentifierAdg(Node root) {
        this.root = root;
        this.currentNode = root;
    }

    /**
     * Replaces the current ADG root and resets the current node to it.
     *
     * @param newRoot the new root node
     */
    public void updateRoot(Node newRoot) {
        this.root = newRoot;
        this.currentNode = newRoot;
    }

    /**
     * Returns the ADG root node.
     *
     * @return the root node
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Returns the current node used during identification.
     *
     * @return current node
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Updates the current node for identification traversal.
     *
     * @param current the node to set as current
     */
    public void updateCurrentNode(Node current) {
        this.currentNode = current;
    }

    /**
     * Returns the next input label from the current node and advances to the corresponding child.
     *
     * @return                       next input label, or {@code null} if the current node is a leaf
     *
     * @throws IllegalStateException if the current node has more than one child
     */
    public String getNextInput() {
        if (this.currentNode.countChildren() > 1)
            throw new IllegalStateException("Cannot get input in an output node");
        if (this.currentNode.isLeaf())
            return null;

        String nextInput = this.currentNode.getChildren().keySet().iterator().next();
        this.currentNode = this.currentNode.getChildren().get(nextInput);
        return nextInput;
    }

    /**
     * Advances the current node based on an observed output label.
     *
     * @param  output the output label used to select the next node
     *
     * @return        the next node, or the current leaf node if already at a leaf
     */
    public Node proceedToNextNodeWithOutput(String output) {
        if (this.currentNode.isLeaf())
            return this.currentNode; // stay in the same leaf node if we are already in a leaf node
        Node nextNode = this.currentNode.getChildren().get(output);
        if (nextNode == null)
            return null;
        this.currentNode = nextNode;
        return nextNode;
    }

    /**
     * Skips a reset transition by advancing with an empty output label.
     *
     * @return the next node after the reset transition
     */
    public Node skipReset() {
        return proceedToNextNodeWithOutput("");
    }

}
