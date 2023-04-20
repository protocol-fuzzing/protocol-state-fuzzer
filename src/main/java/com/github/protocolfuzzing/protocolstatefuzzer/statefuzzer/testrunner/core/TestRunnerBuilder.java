package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

/**
 * Builder Interface for the TestRunner.
 */
public interface TestRunnerBuilder {
    /**
     * Builds a new TestRunner instance.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @return                   a new TestRunner instance
     */
    TestRunner build(TestRunnerEnabler testRunnerEnabler);
}
