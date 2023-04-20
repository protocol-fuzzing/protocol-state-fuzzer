package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

/**
 * Builder Interface for the TimingProbe.
 */
public interface TimingProbeBuilder {

    /**
     * Builds a new TimingProbe instance.
     *
     * @param timingProbeEnabler  the configuration that enables the timing probe testing
     * @return                    a new TimingProbe instance
     */
    TimingProbe build(TimingProbeEnabler timingProbeEnabler);
}
