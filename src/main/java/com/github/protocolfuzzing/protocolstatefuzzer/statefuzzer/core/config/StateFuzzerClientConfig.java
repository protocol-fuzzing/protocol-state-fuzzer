package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

/**
 * The extension of StateFuzzerConfig regarding a client SUL.
 */
@Parameters(commandDescription = "Performs state-fuzzing on a protocol client generating a model of the system")
public class StateFuzzerClientConfig extends StateFuzzerConfigStandard {

    /** Stores the specified SulClientConfig. */
    @ParametersDelegate
    protected SulClientConfig sulClientConfig;

    /**
     * Constructs a new instance from the default super constructor and the parameter.
     *
     * @param sulClientConfig  the {@link SulClientConfig} (sub)class
     */
    public StateFuzzerClientConfig(SulClientConfig sulClientConfig) {
        super();
        this.sulClientConfig = sulClientConfig;
    }


    /**
     * Constructs a new instance from the given parameters.
     *
     * @param learnerConfig      the {@link LearnerConfig} (sub)class
     * @param sulClientConfig    the {@link SulClientConfig} (sub)class
     * @param testRunnerConfig   the {@link TestRunnerConfig} (sub)class
     * @param timingProbeConfig  the {@link TimingProbeConfig} (sub)class
     */
    public StateFuzzerClientConfig(LearnerConfig learnerConfig, SulClientConfig sulClientConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulClientConfig = sulClientConfig;
    }

    @Override
    public SulConfig getSulConfig() {
        return sulClientConfig;
    }

    @Override
    public boolean isFuzzingClient() {
        return true;
    }
}
