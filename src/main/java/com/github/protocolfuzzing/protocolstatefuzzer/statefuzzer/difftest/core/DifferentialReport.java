package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates a test file and a report file from the divergences found by {@link DifferentialOracle}.
 * <p>
 * The test file follows PSFs test file format and each divergence is represented as a test sequence.
 * <p>
 * The report file consist of a header with information about the test,
 * followed by all the divergences, listed in a readable format.
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 */
public class DifferentialReport<I, O> {

    /** Stores the path to write the test file */
    protected Path testFilePath;

    /** Stores the path to write the report file */
    protected Path reportPath;

    /**
     * Construct a new instance from the given parameters
     *
     * @param testFilePath the path to write the test file
     * @param reportPath   the path to write the report file
     */
    public DifferentialReport(Path testFilePath, Path reportPath) {
        this.testFilePath = testFilePath;
        this.reportPath = reportPath;
    }

    /**
     * Writes a test file containing one test sequence per divergence.
     * <p>
     * Each test sequence is structured as follows:
     * A comment identifying the divergence number
     * The witness sequence, one symbol per line
     * A comment documenting the differing outputs of each model
     * reset
     * <p>
     * Example output for a single divergence
     * # Divergence 1
     * CLIENT_HELLO
     * CLIENT_HELLO
     * FINISHED
     * # ModelA output: ALERT_FATAL | ModelB output: SERVER_HELLO
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

    /**
     * Writes a summary report of the differential testing.
     * Contains information about the test and details about each divergence found.
     * <p>
     * Example output:
     * ========== Differential Analysis Report ==========
     * Models compared : modelA.dot vs modelB.dot
     * Number of divergences : 3
     * ==================================================
     * Divergence 1
     * Witness sequence : [CLIENT_HELLO, FINISHED]
     * Model A output : SERVER_HELLO
     * Model B output : TIMEOUT
     * ...
     *
     * @param  divergences the list of divergence records
     * @param  modelAName  the name of model A
     * @param  modelBName  the name of model B
     *
     * @throws IOException if an error occurs when writing to the file
     */
    public void writeReport(List<DivergenceRecord<I, O>> divergences, String modelAName, String modelBName)
        throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(reportPath))) {
            writeReportHeader(writer, divergences, modelAName, modelBName);
            writeDivergenceDetails(writer, divergences);
        }
    }

    /**
     * Writes the header of the differential testing report.
     *
     * @param writer      a printwriter
     * @param divergences the list of divergence records
     * @param modelAName  the name of model A
     * @param modelBName  the name of model B
     */
    private void writeReportHeader(PrintWriter writer, List<DivergenceRecord<I, O>> divergences, String modelAName,
        String modelBName) {
        writer.println("========== Differential Analysis Report ==========");
        writer.printf("Models compared       : %s vs %s%n", modelAName, modelBName);
        writer.printf("Number of divergences : %d%n", divergences.size());
        writer.println("==================================================");
    }

    /**
     * Writes details about each divergence.
     *
     * @param writer      a printwriter
     * @param divergences a list of divergence records
     */
    private void writeDivergenceDetails(PrintWriter writer, List<DivergenceRecord<I, O>> divergences) {
        for (int i = 0; i < divergences.size(); i++) {
            DivergenceRecord<I, O> d = divergences.get(i);
            writer.println();
            writer.println("Divergence " + (i + 1));
            writer.printf("Witness sequence : %s%n", d.getWitnessSequence());
            writer.printf("Model A output   : %s%n", d.getOutputA());
            writer.printf("Model B output   : %s%n", d.getOutputB());
        }
    }
}
