package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

/**
 * Builder interface for the DiffTester.
 */
public interface DiffTesterBuilder {
    /**
     * Builds a new DiffTester instance.
     *
     * @param  diffTesterEnabler the configuration that enables the diff testing
     *
     * @return                   a new DiffTester instance
     */
    DiffTester build(DiffTesterEnabler diffTesterEnabler);
}
