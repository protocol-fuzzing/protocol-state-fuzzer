package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * Data Structure used for storing and querying inputs and outputs.
 * <p>
 * An instance of an Observation Tree represents a node of the tree.
 * <p>
 * Adapted from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>.
 *
 * @param <I> the input type of the observations
 * @param <O> the output type of the observations
 */
public class ObservationTree<I, O> {

    /** Stores the constructor parameter. */
    protected ObservationTree<I, O> parent;

    /** Stores the constructor parameter. */
    protected I parentInput;

    /** Stores the constructor parameter. */
    protected O parentOutput;

    /** Stores the children nodes of this node. */
    protected Map<I, ObservationTree<I, O>> children;

    /** Stores the outputs corresponding to the inputs of this node. */
    protected Map<I, O> outputs;

    /**
     * Constructs a new instance using {@link #ObservationTree(ObservationTree, Object, Object)}
     * with null parameters.
     */
    public ObservationTree() {
        this(null, null, null);
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param parent        the parent observation tree of this node
     * @param parentInput   the parent input
     * @param parentOutput  the parent output
     */
    public ObservationTree(ObservationTree<I, O> parent, I parentInput, O parentOutput) {
        this.children = new HashMap<>();
        this.outputs = new HashMap<>();
        this.parent = parent;
        this.parentInput = parentInput;
        this.parentOutput = parentOutput;
    }

    /**
     *  Return a word of symbols from a symbols list.
     *
     * @param <T>         the type of list contents
     * @param symbolList  the list to be converted
     * @return            the word of symbols
     */
    public static <T> Word<T> toWord(List<T> symbolList) {
        return Word.fromList(symbolList);
    }

    /**
     * Returns the outputs observed from the root of the tree until this node.
     *
     * @return  the outputs observed from the root of the tree until this node
     */
    protected List<O> getOutputChain() {
        if (this.parent == null) {
            return new ArrayList<>();
        }

        List<O> parentChain = this.parent.getOutputChain();
        parentChain.add(parentOutput);
        return parentChain;
    }

    /**
     * Returns the inputs observed from the root of the tree until this node.
     *
     * @return  the inputs observed from the root of the tree until this node
     */
    protected List<I> getInputChain() {
        if (this.parent == null) {
            return new ArrayList<>();
        }

        List<I> parentChain = this.parent.getInputChain();
        parentChain.add(this.parentInput);
        return parentChain;
    }

    /**
     * Add one input and output symbol and traverse the tree to the next node.
     *
     * @param input   the input symbol to be added
     * @param output  the output symbol to be added
     * @return        the next node
     *
     * @throws CacheInconsistencyException  on inconsistency with previous observations input
     */
    public ObservationTree<I, O> addObservation(I input, O output) throws CacheInconsistencyException {
        O previousOutput = this.outputs.get(input);
        boolean createNewBranch = previousOutput == null;

        if (createNewBranch) {
            // input hasn't been queried before, make a new branch for it and traverse
            this.outputs.put(input, output);
            ObservationTree<I, O> child = new ObservationTree<>(this, input, output);
            this.children.put(input, child);
            return child;
        }

        if (!previousOutput.equals(output)) {
            // input is inconsistent with previous observations, throw exception
            List<O> oldOutputChain = this.children.get(input).getOutputChain();
            List<O> newOutputChain = this.getOutputChain();
            List<I> inputChain = this.getInputChain();
            newOutputChain.add(output);
            throw new CacheInconsistencyException(toWord(inputChain), toWord(oldOutputChain), toWord(newOutputChain));
        }

        // input is consistent with previous observations, just traverse
        return this.children.get(input);
    }

    /**
     * Add Observation of Words to the tree using {@link #addObservation(List, List)}.
     *
     * @param inputs   the word of inputs
     * @param outputs  the word of outputs
     *
     * @throws CacheInconsistencyException  on inconsistency between new and stored observations
     */
    public void addObservation(Word<I> inputs, Word<O> outputs) throws CacheInconsistencyException {
        addObservation(inputs.asList(), outputs.asList());
    }

    /**
     * Add Observation of Lists to the tree.
     *
     * @param inputs   the list of inputs
     * @param outputs  the list of outputs
     *
     * @throws CacheInconsistencyException  on inconsistency between new and stored observations
     */
    public void addObservation(List<I> inputs, List<O> outputs) throws CacheInconsistencyException {
        if (inputs.isEmpty() && outputs.isEmpty()) {
            return;
        }

        if (inputs.isEmpty() || outputs.isEmpty()) {
            throw new RuntimeException("Input and output words should have the same length:" + "\n" + inputs + "\n" + outputs);
        }

        I firstInput = inputs.get(0);
        O firstOutput = outputs.get(0);
        try {
            this.addObservation(firstInput, firstOutput)
                .addObservation(inputs.subList(1, inputs.size()), outputs.subList(1, outputs.size()));
        } catch (CacheInconsistencyException e) {
            throw new CacheInconsistencyException(toWord(inputs), e.getOldOutput(), toWord(outputs));
        }
    }

    /**
     * Removes this node from the observation tree.
     */
    public void remove() {
        if (this.parent == null) {
            throw new RuntimeException("Cannot remove root node");
        }

        for (I symbol : this.parent.children.keySet()) {
            if (this == this.parent.children.get(symbol)) {
                this.parent.children.remove(symbol);
                this.parent.outputs.remove(symbol);
                break;
            }
        }
    }

    /**
     * Removes the given word sequence starting from this node using {@link #remove(List)}.
     *
     * @param accessSequence  the word sequence to be removed
     */
    public void remove(Word<I> accessSequence) {
        remove(accessSequence.asList());
    }

    /**
     * Removes the given list sequence starting from this node.
     * <p>
     * If the accessSequence is empty then this node is removed.
     *
     * @param accessSequence  the list sequence to be removed
     *
     * @throws RemovalException  if the sequence cannot be removed
     */
    public void remove(List<I> accessSequence) throws RemovalException {
        if (accessSequence.isEmpty()) {
            this.remove();
            return;
        }

        ObservationTree<I, O> child = this.children.get(accessSequence.get(0));

        if (child == null) {
            throw new RemovalException("Cannot remove branch which is not present for input\n" + accessSequence);
        }

        try {
            child.remove(accessSequence.subList(1, accessSequence.size()));
        } catch (RemovalException e) {
            throw new RemovalException("Cannot remove branch which is not present for input\n" + accessSequence);
        }
    }

    /**
     * Answers the given query only if the whole input word is stored in the tree.
     *
     * @param word  the input word
     * @return      an answer only if the whole input word is stored in the tree,
     *              otherwise null
     */
    @Nullable public Word<O> answerQuery(Word<I> word) {
        List<I> inputChain = word.asList();
        List<O> outputChain = answerInputChain(inputChain, false);
        if (outputChain != null) {
            return toWord(outputChain);
        }
        return null;
    }

    /**
     * Answers the given query with the option to allow incomplete answers.
     * <p>
     * Incomplete resolution captures the case when the input word is not
     * completely stored in the tree.
     *
     * @param word                   the input word to be answered
     * @param allowIncompleteAnswer  {@code true} to enable incomplete answers
     * @return                       an answer for the longest prefix stored in
     *                               the cache if allowIncompleteAnswer is
     *                               {@code true} else null
     */
    @Nullable public Word<O> answerQuery(Word<I> word, boolean allowIncompleteAnswer) {
        List<I> inputChain = word.asList();
        List<O> outputChain = answerInputChain(inputChain, allowIncompleteAnswer);
        if (outputChain != null) {
            return toWord(outputChain);
        }
        return null;
    }

    /**
     * Answers the given list of inputs with the option to allow incomplete answers.
     * <p>
     * Incomplete resolution captures the case when the input word is not
     * completely stored in the tree.
     *
     * @param inputs                 the list of inputs to be answered
     * @param allowIncompleteAnswer  {@code true} to enable incomplete answers
     * @return                       the list of answers or null
     */
    @Nullable public List<O> answerInputChain(List<I> inputs, boolean allowIncompleteAnswer) {
        if (inputs.isEmpty()) {
            return new ArrayList<O>();
        }

        I input = inputs.get(0);
        O output = outputs.get(input);
        ObservationTree<I, O> nextObservationTree = this.children.get(input);

        if (output == null || nextObservationTree == null) {
            return allowIncompleteAnswer ? Collections.emptyList() : null;
        }

        List<I> nextInputs = inputs.subList(1, inputs.size());
        List<O> nextOutputs = nextObservationTree.answerInputChain(nextInputs, allowIncompleteAnswer);

        if (nextOutputs == null) {
            return null;
        }

        List<O> outputs = new ArrayList<>(inputs.size());
        outputs.add(output);
        outputs.addAll(nextOutputs);
        return outputs;
    }
}
