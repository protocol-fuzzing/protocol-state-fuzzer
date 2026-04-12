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

    public DivergenceRecord(List<I> witnessSequence, I divergingInput, O outputA, O outputB) {
        this.witnessSequence = witnessSequence;
        this.divergingInput = divergingInput;
        this.outputA = outputA;
        this.outputB = outputB;
    }

    public List<I> getWitnessSequence() {
        return witnessSequence;
    }

    public I getDivergingInput() {
        return divergingInput;
    }

    public O getOutputA() {
        return outputA;
    }

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
