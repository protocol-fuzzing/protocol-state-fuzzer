package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

/**
 * Interface for the differential testing process.
 */
public interface DiffTester {
    /**
     * Runs the implemented diff tester
     */
    public DiffTestResult run();
}
