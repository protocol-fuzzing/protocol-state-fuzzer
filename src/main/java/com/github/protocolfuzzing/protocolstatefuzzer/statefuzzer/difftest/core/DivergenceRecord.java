package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.util.List;

/**
 * Represets a divergence between two models
 *
 * @param <I> the type of input
 * @param <O> the type of output
 */
public class DivergenceRecord<I, O> {
    /** Stores the witness sequence */
    private final List<I> witnessSequence;

    /** Stores the input creating the divergnece */
    private final I divergingInput;

    /** Stores the output of modelA */
    private final O outputA;

    /** Stores the output of modelB */
    private final O outputB;

    /**
     * Constructs a new instance for the given parameters
     *
     * @param witnessSequence the witness sequence
     * @param divergingInput  the input creating the divergence
     * @param outputA         output of modelA
     * @param outputB         output of modelB
     */
    public DivergenceRecord(List<I> witnessSequence, I divergingInput, O outputA, O outputB) {
        this.witnessSequence = witnessSequence;
        this.divergingInput = divergingInput;
        this.outputA = outputA;
        this.outputB = outputB;
    }

    /** Returns the witness sequence */
    public List<I> getWitnessSequence() {
        return witnessSequence;
    }

    /** Returns the input creating the divergence */
    public I getDivergingInput() {
        return divergingInput;
    }

    /** Returns the output of modelA */
    public O getOutputA() {
        return outputA;
    }

    /**
     * Returns the output of modelB
     */
    public O getOutputB() {
        return outputB;
    }

    @Override
    public String toString() {
        return "Divergence found:\n"
            + " Witness: " + witnessSequence + "\n"
            + " Input    " + divergingInput + "\n"
            + " Model A  " + outputA + "\n"
            + " Model B  " + outputB;
    }
}
