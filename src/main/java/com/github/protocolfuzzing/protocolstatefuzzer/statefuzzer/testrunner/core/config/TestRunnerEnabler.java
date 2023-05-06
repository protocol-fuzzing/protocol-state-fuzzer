package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;

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
     * Returns the SulConfig.
     *
     * @return  the SulConfig
     */
    SulConfig getSulConfig();

    /**
     * Returns the TestRunnerConfig.
     *
     * @return  the TestRunnerConfig
     */
    TestRunnerConfig getTestRunnerConfig();
}
