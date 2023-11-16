package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import net.automatalib.word.Word;

import java.io.Serial;

/**
 * Exception used by {@link MultipleRunsSULOracle} and subclasses.
 * <p>
 * It contains the full input for which non-determinism was observed, as well as
 * the full new output and the (possibly shorter) old output with which it
 * disagrees.
 */
public class NonDeterminismException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The input before the specified one in the constructor. */
    protected Word<?> precedingInput;

    /** Stores the constructor parameter. */
    protected Word<?> input;

    /** Stores the constructor parameter. */
    protected Word<?> oldOutput;

    /** Stores the constructor parameter. */
    protected Word<?> newOutput;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param input      the input for which non-determinism was observed
     * @param oldOutput  the old output corresponding to the input
     * @param newOutput  the new output corresponding to the input and is different from oldOutput
     */
    public NonDeterminismException(Word<?> input, Word<?> oldOutput, Word<?> newOutput) {
        this.input = input;
        this.oldOutput = oldOutput;
        this.newOutput = newOutput;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param message    the message related to the exception
     * @param input      the input for which non-determinism was observed
     * @param oldOutput  the old output corresponding to the input
     * @param newOutput  the new output corresponding to the input and is different from oldOutput
     */
    public NonDeterminismException(String message, Word<?> input, Word<?> oldOutput, Word<?> newOutput) {
        super(message);
        this.input = input;
        this.oldOutput = oldOutput;
        this.newOutput = newOutput;
    }

    /**
     * Returns the stored {@link #oldOutput}.
     *
     * @return  the stored {@link #oldOutput}
     */
    public Word<?> getOldOutput() {
        return this.oldOutput;
    }

    /**
     * Returns the stored {@link #newOutput}.
     *
     * @return  the stored {@link #newOutput}
     */
    public Word<?> getNewOutput() {
        return this.newOutput;
    }

    /**
     * Stores the given preceding input in {@link #precedingInput}.
     *
     * @param precedingInput  the input to be set
     */
    public void setPrecedingInput(Word<?> precedingInput) {
        this.precedingInput = precedingInput;
    }

    /**
     * Returns the shortest sub-word of the input word which causes non-determinism.
     *
     * @return  the shortest sub-word of the input word which causes non-determinism.
     */
    public Word<?> getShortestInconsistentInput() {
        int indexOfInconsistency = 0;
        while (oldOutput.getSymbol(indexOfInconsistency).equals(
            newOutput.getSymbol(indexOfInconsistency))) {
            indexOfInconsistency++;
        }
        return this.input.subWord(0, indexOfInconsistency + 1);
    }

    /**
     * Makes the instance more compact by replacing {@link #input} with the result of
     * {@link #getShortestInconsistentInput()} and shortening the length of {@link #oldOutput}
     * and {@link #newOutput} to match the length of the new {@link #input}.
     *
     * @return  this instance with the {@link #input}, {@link #oldOutput} and {@link #newOutput} changed
     */
    public NonDeterminismException makeCompact() {
        this.input = getShortestInconsistentInput();
        this.oldOutput = this.oldOutput.prefix(input.length());
        this.newOutput = this.newOutput.prefix(input.length());
        return this;
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of this instance
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Non-determinism detected\n");
        sb.append("full input:\n");
        sb.append(input);
        sb.append("\nfull new output:\n");
        sb.append(newOutput);
        sb.append("\nold output:\n");
        sb.append(oldOutput);
        if (precedingInput != null)
        sb.append("\npreceding input:\n");
        sb.append(precedingInput);
        return sb.toString();
    }
}
