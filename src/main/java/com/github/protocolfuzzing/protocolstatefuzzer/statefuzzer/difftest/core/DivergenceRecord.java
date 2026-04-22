package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.util.List;

/**
 * Records a divergence between two Mealy machines on a specific input sequence.
 * <p>
 * A divergence occures when two models produce differrnt outputs for the same input.
 * The witness sequence is the full sequence of inputs that exposes this difference.
 *
 * @param <I> the type of input
 * @param <O> the type of output
 */
public class DivergenceRecord<I, O> {
    /** The input sequence that witnesses the divergence. */
    private final List<I> witnessSequence;

    /** The output produced by model A on the final input of the witness sequence. */
    private final O outputA;

    /** The output produced by model B on the final input of the witness sequence. */
    private final O outputB;

    /**
     * Constructs a new instance for the given parameters
     *
     * @param witnessSequence the witness sequence
     * @param outputA         output of modelA
     * @param outputB         output of modelB
     */
    public DivergenceRecord(List<I> witnessSequence, O outputA, O outputB) {
        this.witnessSequence = witnessSequence;
        this.outputA = outputA;
        this.outputB = outputB;
    }

    /**
     * Returns the witness sequence
     *
     * @return the witness sequence
     */
    public List<I> getWitnessSequence() {
        return witnessSequence;
    }

    /**
     * Returns the output of modelA
     *
     * @return the output of modelA
     */
    public O getOutputA() {
        return outputA;
    }

    /**
     * Returns the output of modelB
     *
     * @return the output of mdoelB
     */
    public O getOutputB() {
        return outputB;
    }

    @Override
    public String toString() {
        return "Divergence found:\n"
            + " Witness: " + witnessSequence + "\n"
            + " Model A  " + outputA + "\n"
            + " Model B  " + outputB;
    }
}
