package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

/**
 * Builder Interface for the TestRunner.
 */
public interface TestRunnerBuilder<S, I, O extends MapperOutput<O>> {
    /**
     * Builds a new TestRunner instance.
     *
     * @param testRunnerEnabler  the configuration that enables the testing
     * @return                   a new TestRunner instance
     */
    TestRunner<S, I, O> build(TestRunnerEnabler testRunnerEnabler);
}
