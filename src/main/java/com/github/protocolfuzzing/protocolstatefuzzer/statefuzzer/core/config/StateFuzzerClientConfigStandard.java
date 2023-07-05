package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

/**
 * The standard StateFuzzer configuration of a client SUL.
 */
@Parameters(commandDescription = "Performs state-fuzzing on a protocol client generating a model of the system")
public class StateFuzzerClientConfigStandard extends StateFuzzerConfigStandard implements StateFuzzerClientConfig {

    /** Stores the specified SulClientConfig. */
    @ParametersDelegate
    protected SulClientConfig sulClientConfig;

    /**
     * Constructs a new instance from the default super constructor and the parameter.
     *
     * @param sulClientConfig  the non-null {@link SulClientConfig} implementing class
     */
    public StateFuzzerClientConfigStandard(SulClientConfig sulClientConfig) {
        super();
        this.sulClientConfig = sulClientConfig;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param learnerConfig      the {@link LearnerConfig} implementing class
     * @param sulClientConfig    the {@link SulClientConfig} implementing class
     * @param testRunnerConfig   the {@link TestRunnerConfig} implementing class
     * @param timingProbeConfig  the {@link TimingProbeConfig} implementing class
     */
    public StateFuzzerClientConfigStandard(LearnerConfig learnerConfig, SulClientConfig sulClientConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulClientConfig = sulClientConfig;
    }

    @Override
    public SulConfig getSulConfig() {
        return sulClientConfig;
    }
}
