package io.github.protocolfuzzing.protocolstatefuzzer.e2e;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The work directory of one end-to-end run of a scenario, with the files the
 * scenario's args files expect in it: the alphabet, {@value #ALPHABET_FILE},
 * written by the subclass, and the tests, {@value #TESTS_FILE}, written by
 * {@link #writeTests()}.
 */
public abstract class ScenarioRun {

    public static final String ALPHABET_FILE = "alphabet.xml";

    /** The test file written by {@link #writeTests()}, in the work directory. */
    public static final String TESTS_FILE = "tests.txt";

    /** The length of the longest test written by {@link #writeTests()}. */
    public static final int TEST_LENGTH = 3;

    private final Scenario scenario;
    private final Path workDir;

    /**
     * Creates the run of a scenario.
     *
     * @param scenario the scenario to run
     * @param workDir  an empty directory for the run's files
     */
    protected ScenarioRun(Scenario scenario, Path workDir) {
        this.scenario = scenario;
        this.workDir = workDir;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Path getOutputDir() {
        return workDir.resolve("output");
    }

    /**
     * Returns the steps the tests are made of, each as the test file writes
     * it, e.g. {@code SREQ} or {@code SREQ[0]}.
     *
     * @return the steps
     */
    protected abstract List<String> testSteps();

    /**
     * Writes every sequence of up to {@value #TEST_LENGTH} {@link #testSteps()}
     * as a test file in the work directory, one test per line, separated by
     * {@code reset}.
     *
     * @return the test file
     */
    public Path writeTests() {
        List<String> tests = new ArrayList<>(List.of(""));
        List<String> lines = new ArrayList<>();
        for (int length = 1; length <= TEST_LENGTH; length++) {
            List<String> longer = new ArrayList<>();
            for (String test: tests) {
                for (String step: testSteps()) {
                    longer.add(test.isEmpty() ? step : test + " " + step);
                }
            }
            tests = longer;
            lines.addAll(tests);
        }

        Path file = workDir.resolve(TESTS_FILE);
        try {
            Files.write(file, List.of(String.join(System.lineSeparator() + "reset" + System.lineSeparator(), lines)),
                StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }

    /**
     * Returns how many tests {@link #writeTests()} writes for a number of
     * steps: every sequence of 1 to {@value #TEST_LENGTH} of them.
     *
     * @param  steps the number of steps
     *
     * @return       the number of tests
     */
    public static int testCount(int steps) {
        int tests = 0;
        for (int length = 1, sequences = steps; length <= TEST_LENGTH; length++, sequences *= steps) {
            tests += sequences;
        }
        return tests;
    }

    /**
     * Returns how many times the scenario's {@value ScenarioCli#TEST} args
     * file runs each test, its {@code -times}.
     *
     * @return the number of runs of each test
     */
    public int testTimes() {
        return Integer.parseInt(ScenarioCli.optionValue(scenario.requireFile(ScenarioCli.TEST), "-times"));
    }

    /**
     * Builds a command running one of the scenario's args files, defining
     * {@value ScenarioCli#SCENARIOS_DIR} and {@value ScenarioCli#WORK_DIR}.
     *
     * @param  argsFile   the args file, relative to the scenario directory
     * @param  properties further properties to define
     *
     * @return            the command-line arguments
     */
    public String[] command(String argsFile, Map<String, String> properties) {
        Map<String, String> all = new LinkedHashMap<>();
        all.put(ScenarioCli.SCENARIOS_DIR, scenariosDir().toString());
        all.put(ScenarioCli.WORK_DIR, workDir.toString());
        all.putAll(properties);
        return ScenarioCli.command(scenario.requireFile(argsFile), all);
    }

    /**
     * Builds a command running one of the scenario's args files.
     *
     * @param  argsFile the args file, relative to the scenario directory
     *
     * @return          the command-line arguments
     */
    public String[] command(String argsFile) {
        return command(argsFile, Map.of());
    }

    /** Returns the directory of all scenarios of the kind, holding the scenario's protocol directory. */
    private Path scenariosDir() {
        Path protocolDir = Objects.requireNonNull(scenario.getDir().getParent(), "no protocol directory");
        return Objects.requireNonNull(protocolDir.getParent(), "no scenarios directory");
    }
}
