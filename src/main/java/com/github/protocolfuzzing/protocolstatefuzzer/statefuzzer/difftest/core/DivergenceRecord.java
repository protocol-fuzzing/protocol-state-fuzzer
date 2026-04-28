package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.util.List;

/**
 * Records a divergence between two Mealy machines on a specific input sequence.
 * <p>
 * A divergence occurs when two models produce different outputs for the same input.
 * The witness sequence is the full sequence of inputs that exposes this difference.
 *
 * @param <I>             the type of input
 * @param <O>             the type of output
 * @param witnessSequence the input sequence that witnesses the divergence
 * @param outputA         the output produced by model A on the final input of the witness sequence
 * @param outputB         the output produced by model B on the final input of the witness sequence
 */
public record DivergenceRecord<I, O>(List<I> witnessSequence, O outputA, O outputB) {}
