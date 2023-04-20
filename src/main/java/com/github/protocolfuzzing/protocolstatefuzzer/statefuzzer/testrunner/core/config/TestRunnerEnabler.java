package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigProvider;

/**
 * Interface that enables testing with the TestRunner by extending the necessary interfaces.
 */
public interface TestRunnerEnabler extends TestRunnerConfigProvider, LearnerConfigProvider, SulConfigProvider {
}
