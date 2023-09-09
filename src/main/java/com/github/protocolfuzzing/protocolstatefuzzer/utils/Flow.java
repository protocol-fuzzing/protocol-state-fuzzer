package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Specifies a flow, which is a word of input symbols with their corresponding
 * word of output symbols with 1:1 correspondence between input and output symbols
 * in the words.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <F>  the type of flow
 */
public abstract class Flow<I, O, F extends Flow<I, O, F>> {

    /** The word of input symbols. */
    protected Word<I> inputWord;

    /** The word of output symbols. */
    protected Word<O> outputWord;

    /** Indicates if the flow starts from the initial state or not. */
    protected boolean fromStart;

    /**
     * Constructs a new instance with {@link #inputWord} and {@link #outputWord}
     * equal to the epsilon word and {@link #fromStart} equal to {@code false}.
     */
    public Flow() {
        this.inputWord = Word.epsilon();
        this.outputWord = Word.epsilon();
        this.fromStart = false;
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Every input in the inputWord corresponds to a single output symbol in
     * the outputWord.
     *
     * @param inputWord   the word containing the input symbols
     * @param outputWord  the word containing the output symbols
     * @param fromStart   indicates if the flow starts from the initial state
     *
     * @throws NullPointerException  if inputWord or outputWord are null
     *                               or if they have different lengths
     */
    public Flow(Word<I> inputWord, Word<O> outputWord, boolean fromStart) {
        if (inputWord == null) {
            throw new NullPointerException("The provided inputWord is null");
        }

        if (outputWord == null) {
            throw new NullPointerException("The provided outputWord is null");
        }

        if (inputWord.length() != outputWord.length()) {
            throw new NullPointerException("The provided inputWord and outputWord have different lengths");
        }

        this.inputWord = inputWord;
        this.outputWord = outputWord;
        this.fromStart = fromStart;
    }

    /**
     * Returns the word of input symbols.
     *
     * @return  the word of input symbols
     */
    public Word<I> getInputWord() {
        return inputWord;
    }

    /**
     * Returns the word of output symbols.
     *
     * @return  the word of output symbols
     */
    public Word<O> getOutputWord() {
        return outputWord;
    }

    /**
     * Returns {@code true} if the flow starts from the initial state.
     *
     * @return  {@code true} if the flow starts from the initial state
     */
    public boolean isFromStart() {
        return fromStart;
    }

    /**
     * Provides the input symbol of the inputWord at the given index.
     *
     * @param index  the index of the input symbol in the inputWord
     * @return       the requested input symbol
     *
     * @throws IndexOutOfBoundsException  if there is no such index
     */
    public I getInput(int index) {
        return inputWord.getSymbol(index);
    }

    /**
     * Provides the output symbol of the outputWord at the given index.
     *
     * @param index  the index of the output symbol in the outputWord
     * @return       the requested output symbol
     *
     * @throws IndexOutOfBoundsException  if there is no such index
     */
    public O getOutput(int index) {
        return outputWord.getSymbol(index);
    }

    /**
     * Provides a stream of input, output symbol pairs.
     *
     * @return  the input, output symbol pair stream
     */
    public Stream<Pair<I, O>> getInputOutputStream() {
        Stream.Builder<Pair<I, O>> builder = Stream.builder();
        for (int i = 0; i < getLength(); i++) {
            builder.add(Pair.of(inputWord.getSymbol(i), outputWord.getSymbol(i)));
        }
        return builder.build();
    }

    /**
     * Provides an iterable of input, output symbol pairs.
     *
     * @return  the input, output symbol pair iterable
     */
    public Iterable<Pair<I, O>> getInputOutputIterable() {
        return getInputOutputStream().collect(Collectors.toList());
    }

    /**
     * Appends an input and output symbol to current flow using {@link #build}.
     *
     * @param input   the input symbol
     * @param output  the output symbol corresponding to the input symbol
     * @return        the flow containing the given input, output symbols
     */
    public F append(I input, O output) {
        return build(inputWord.append(input), outputWord.append(output), fromStart);
    }

    /**
     * Concatenates this flow with another flow, only if this flow starts from
     * the initial state and the other one does not using {@link #build}.
     *
     * @param other  the other flow to be concatenated with the current one
     * @return       the concatenated flow, consisting of both flows
     *
     * @throws RuntimeException  if the other flow starts from the initial state
     *                           or the current one does not start from the
     *                           initial state (indicated by {@link #fromStart})
     */
    public F concat(F other) {
        if (!this.isFromStart()) {
            throw new RuntimeException("The current flow does not start from the initial state");
        }

        if (other.isFromStart()) {
            throw new RuntimeException("The provided other flow starts from the initial state");
        }

        return build(inputWord.concat(other.getInputWord()), outputWord.concat(other.getOutputWord()), fromStart);
    }

    /**
     * Generates a flow that is the prefix of a given length of the
     * current flow using {@link #build}.
     *
     * @param length  the length of the flow's prefix
     * @return        the desired prefix-flow
     *
     * @throws RuntimeException  if the provided length is greater than the
     *                           length of the current flow
     */
    public F prefix(int length) {
        if (length > getLength()) {
            throw new RuntimeException("Requested prefix length is greater than flow length");
        }

        return build(inputWord.prefix(length), outputWord.prefix(length), fromStart);
    }

    /**
     * Builds a flow from the initial parameters, a method to be overridden.
     *
     * @param inputWord      the word containing the input symbols
     * @param outputWord     the word containing the output symbols
     * @param fromStart      indicates if the flow starts from the initial state
     * @return               the built flow
     */
    protected abstract F build(Word<I> inputWord, Word<O> outputWord, boolean fromStart);

    /**
     * Returns the length of the flow, which equals to the length of either
     * the inputWord or the outputWord.
     *
     * @return  the length of the flow, which equals to the length of either
     *          the inputWord or the outputWord.
     */
    public int getLength() {
        return inputWord.length();
    }

    /**
     * Provides a single-line string representation of the current flow.
     *
     * @return  the string representation
     */
    public String toCompactString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Flow: ");
        for (int i = 0; i < getLength(); i++) {
            builder.append(inputWord.getSymbol(i)).append("/").append(outputWord.getSymbol(i)).append(' ');
        }
        return builder.toString();
    }

    /**
     * Provides a three-line string representation of the current flow.
     *
     * @return  the string representation
     */
    @Override
    public String toString() {
        return String.format("Flow:%n  inputs: %s%n  outputs: %s%n", inputWord, outputWord);
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of this instance
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fromStart ? 1231 : 1237);
        result = prime * result + ((inputWord == null) ? 0 : inputWord.hashCode());
        result = prime * result + ((outputWord == null) ? 0 : outputWord.hashCode());
        return result;
    }

    /**
     * Overrides the default method.
     *
     * @return  {@code true} if this instance is equal to the provided object
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (! (obj instanceof Flow)) {
            return false;
        }

        Flow<?, ?, ?> other = (Flow<?, ?, ?>) obj;

        if (fromStart != other.fromStart) {
            return false;
        }

        if (inputWord == null && other.inputWord != null) {
            return false;
        }

        if (inputWord != null && !inputWord.equals(other.inputWord)) {
            return false;
        }

        if (outputWord == null) {
            return other.outputWord == null;
        } else {
            return outputWord.equals(other.outputWord);
        }
    }
}
