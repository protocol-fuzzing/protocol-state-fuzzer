package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

@Parameters(commandDescription = "Performs state-fuzzing on a protocol client generating a model of the system")
public class StateFuzzerClientConfig extends StateFuzzerConfig {
	@ParametersDelegate
	protected SulClientConfig sulClientConfig;

	public StateFuzzerClientConfig(SulClientConfig sulClientConfig) {
		super();
		this.sulClientConfig = sulClientConfig;
	}

	public StateFuzzerClientConfig(LearnerConfig learnerConfig, SulClientConfig sulClientConfig,
								   TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {
		super(learnerConfig, testRunnerConfig, timingProbeConfig);
		this.sulClientConfig = sulClientConfig;
	}

	@Override
	public SulConfig getSulConfig() {
		return sulClientConfig;
	}

	public boolean isFuzzingClient() {
		return true;
	}

}
