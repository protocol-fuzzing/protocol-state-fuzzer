package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes a test file with one test sequence for each divergence
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
     * Writes a test file containing one test sequence per divergence,
     * consisting of the witness sequnece followed by the diverging input.
     *
     * @param  divergneces the list of divergence records
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

                writer.println(d.getDivergingInput());
                writer.println("# ModelA output: " + d.getOutputA() + " | ModelB output: " + d.getOutputB());
                writer.println("reset");
            }
        }
    }
}
