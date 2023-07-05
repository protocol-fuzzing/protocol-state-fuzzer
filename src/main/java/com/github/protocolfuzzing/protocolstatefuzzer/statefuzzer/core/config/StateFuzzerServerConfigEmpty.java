package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

/**
 * The empty StateFuzzer configuration of a server SUL.
 */
@Parameters(commandDescription = "Performs state-fuzzing on a protocol server generating a model of the system")
public class StateFuzzerServerConfigEmpty extends StateFuzzerConfigEmpty implements StateFuzzerServerConfig {

    /** Stores the specified SulServerConfig. */
    @ParametersDelegate
    protected SulServerConfig sulServerConfig;

    /**
     * Constructs a new instance from the default super constructor and the parameter.
     *
     * @param sulServerConfig  the {@link SulServerConfig} implementing class
     */
    public StateFuzzerServerConfigEmpty(SulServerConfig sulServerConfig) {
        super();
        this.sulServerConfig = sulServerConfig;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param learnerConfig      the {@link LearnerConfig} implementing class
     * @param sulServerConfig    the {@link SulServerConfig} implementing class
     * @param testRunnerConfig   the {@link TestRunnerConfig} implementing class
     * @param timingProbeConfig  the {@link TimingProbeConfig} implementing class
     */
    public StateFuzzerServerConfigEmpty(LearnerConfig learnerConfig, SulServerConfig sulServerConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulServerConfig = sulServerConfig;
    }

    @Override
    public SulConfig getSulConfig() {
        return sulServerConfig;
    }
}
