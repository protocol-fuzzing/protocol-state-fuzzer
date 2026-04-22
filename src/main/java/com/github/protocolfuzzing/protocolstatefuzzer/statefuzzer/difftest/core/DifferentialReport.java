package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates a test file from the divergences found by {@link DifferentialOracle},
 * where each divergence is represented as an test sequence.
 * <p>
 * The generated test file follows the PSF test file format.
 * Each test sequence consists of a witness exposing the divergence.
 * Each sequence is preceded by a comment identifying the divergence and
 * followed by a comment documenting the differing outputs of the two models,
 * and a reset code at the end of each sequence.
 * <p>
 * Example output for a single divergence
 * # Divergence 1
 * CLIENT_HELLO
 * CLIENT_HELLO
 * FINISHED
 * # ModelA output: ALERT_FATAL | ModelB output: SERVER_HELLO
 * reset
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 */
public class DifferentialReport<I, O> {

    /** Stores the path to write the test file */
    protected Path testFilePath;

    /**
     * Construct a new instance from the given parameters
     *
     * @param testFilePath the path to write the test file
     */
    public DifferentialReport(Path testFilePath) {
        this.testFilePath = testFilePath;
    }

    /**
     * Writes a test file containing one test sequence per divergence.
     * <p>
     * Each test sequence is structured as follows:
     * A comment identifying the divergence number
     * The witness sequence, one symbol per line
     * A comment documenting the differing outputs of each model
     * reset
     *
     * @param  divergences the list of divergence records
     *
     * @throws IOException if an error occurs when writing to the file
     */
    public void writeTestFile(List<DivergenceRecord<I, O>> divergences) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(testFilePath))) {
            for (int i = 0; i < divergences.size(); i++) {
                DivergenceRecord<I, O> d = divergences.get(i);
                writer.println("# Divergence " + (i + 1));

                for (I symbol: d.getWitnessSequence()) {
                    writer.println(symbol);
                }

                writer.println("# ModelA output: " + d.getOutputA() + " | ModelB output: " + d.getOutputB());
                writer.println("reset");
            }
        }
    }
}
