package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

/**
 * Builder Interface for the TestRunner.
 */
public interface TestRunnerBuilder<I, O extends AbstractOutput> {
    /**
     * Builds a new TestRunner instance.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @return                   a new TestRunner instance
     */
    TestRunner<I, O> build(TestRunnerEnabler testRunnerEnabler);
}
