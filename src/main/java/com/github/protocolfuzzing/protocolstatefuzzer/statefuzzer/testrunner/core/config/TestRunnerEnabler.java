package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigProvider;

public interface TestRunnerEnabler extends TestRunnerConfigProvider, LearnerConfigProvider, SulConfigProvider {
}
