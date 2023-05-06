package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

/**
 * Interface that enables testing with the TimingProbe by extending the necessary interface.
 */
public interface TimingProbeEnabler extends TestRunnerEnabler {

    /**
     * Returns the TimingProbeConfig.
     *
     * @return  the TimingProbeConfig
     */
    TimingProbeConfig getTimingProbeConfig();
}
