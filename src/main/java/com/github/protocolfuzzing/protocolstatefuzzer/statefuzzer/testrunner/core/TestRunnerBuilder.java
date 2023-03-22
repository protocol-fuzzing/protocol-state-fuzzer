package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

public interface TestRunnerBuilder {
    TestRunner build(TestRunnerEnabler testRunnerEnabler);
}
