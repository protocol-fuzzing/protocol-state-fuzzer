package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

/**
 * Interface for the differential testing process.
 */
public interface DiffTester {
    /**
     * Runs the implemented diff tester
     *
     * @return the result of the diff testing
     */
    public DiffTestResult run();
}
