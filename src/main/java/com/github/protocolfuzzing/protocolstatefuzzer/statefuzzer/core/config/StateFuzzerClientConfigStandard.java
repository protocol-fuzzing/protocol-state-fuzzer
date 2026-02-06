package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

/**
 * The standard StateFuzzer configuration of a client SUL.
 */
@Parameters(commandDescription = "Performs state-fuzzing on a protocol client generating a model of the system")
public class StateFuzzerClientConfigStandard extends StateFuzzerConfigStandard implements StateFuzzerClientConfig {

    /** Stores the specified SULClientConfig. */
    @ParametersDelegate
    protected SULClientConfig sulClientConfig;

    /**
     * Constructs a new instance from the default super constructor and the parameter.
     * <p>
     * If the provided parameter is null, then the corresponding config is
     * initialized with a new empty corresponding configuration.
     *
     * @param sulClientConfig  the {@link SULClientConfig} implementing class
     */
    public StateFuzzerClientConfigStandard(SULClientConfig sulClientConfig) {
        super();
        this.sulClientConfig = sulClientConfig == null ? new SULClientConfig(){} : sulClientConfig;
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If any provided parameter is null, then the corresponding config is
     * initialized with a new empty corresponding configuration.
     *
     * @param learnerConfig      the {@link LearnerConfig} implementing class
     * @param sulClientConfig    the {@link SULClientConfig} implementing class
     * @param testRunnerConfig   the {@link TestRunnerConfig} implementing class
     * @param timingProbeConfig  the {@link TimingProbeConfig} implementing class
     */
    public StateFuzzerClientConfigStandard(LearnerConfig learnerConfig, SULClientConfig sulClientConfig,
        TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {

        super(learnerConfig, testRunnerConfig, timingProbeConfig);
        this.sulClientConfig = sulClientConfig == null ? new SULClientConfig(){} : sulClientConfig;
    }

    @Override
    public SULConfig getSULConfig() {
        return sulClientConfig;
    }
}
