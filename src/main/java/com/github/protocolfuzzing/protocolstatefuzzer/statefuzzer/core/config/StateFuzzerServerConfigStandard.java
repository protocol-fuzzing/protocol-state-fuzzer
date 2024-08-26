package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

/**
 * The standard StateFuzzer configuration of a server SUL.
 */
@Parameters(commandDescription = "Performs state-fuzzing on a protocol server generating a model of the system")
public class StateFuzzerServerConfigStandard extends StateFuzzerConfigStandard implements StateFuzzerServerConfig {

    /** Stores the specified SulServerConfig. */
    @ParametersDelegate
    protected SulServerConfig sulServerConfig;

    /**
     * Constructs a new instance from the default super constructor and the parameter.
     * <p>
     * If the provided parameter is null, then the corresponding config is
     * initialized with a new empty corresponding configuration.
     *
     * @param sulServerConfig  the {@link SulServerConfig} implementing class
     */
    public StateFuzzerServerConfigStandard(SulServerConfig sulServerConfig) {
        super();
        this.sulServerConfig = sulServerConfig == null ? new SulServerConfig(){} : sulServerConfig;
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If any provided parameter is null, then the corresponding config is
     * initialized with a new empty corresponding configuration.
     *
     * @param learnerConfig      the {@link LearnerConfig} implementing class
     * @param sulServerConfig    the {@link SulServerConfig} implementing class
     * @param testRunnerConfig   the {@link TestRunnerConfig} implementing class
     * @param timingProbeConfig  the {@link TimingProbeConfig} implementing class
     */
    public StateFuzzerServerConfigStandard(LearnerConfig learnerConfig, SulServerConfig sulServerConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulServerConfig = sulServerConfig == null ? new SulServerConfig(){} : sulServerConfig;
    }

    @Override
    public SulConfig getSulConfig() {
        return sulServerConfig;
    }
}
