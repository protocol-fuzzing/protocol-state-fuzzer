package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;

/**
 * Interface that enables testing with the TestRunner by extending the necessary interfaces.
 */
public interface TestRunnerEnabler {

    /**
     * Returns the LearnerConfig.
     *
     * @return  the LearnerConfig
     */
    LearnerConfig getLearnerConfig();

    /**
     * Returns the SULConfig.
     *
     * @return  the SULConfig
     */
    SULConfig getSULConfig();

    /**
     * Returns the TestRunnerConfig.
     *
     * @return  the TestRunnerConfig
     */
    TestRunnerConfig getTestRunnerConfig();
}
