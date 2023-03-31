package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

@Parameters(commandDescription = "Performs state-fuzzing on a protocol server generating a model of the system")
public class StateFuzzerServerConfig extends StateFuzzerConfig {
    @ParametersDelegate
    protected SulServerConfig sulServerConfig;

    public StateFuzzerServerConfig(SulServerConfig sulServerConfig) {
        super();
        this.sulServerConfig = sulServerConfig;
    }

    public StateFuzzerServerConfig(LearnerConfig learnerConfig, SulServerConfig sulServerConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulServerConfig = sulServerConfig;
    }

    @Override
    public SulConfig getSulConfig() {
        return sulServerConfig;
    }

    public boolean isFuzzingClient() {
        return false;
    }
}
