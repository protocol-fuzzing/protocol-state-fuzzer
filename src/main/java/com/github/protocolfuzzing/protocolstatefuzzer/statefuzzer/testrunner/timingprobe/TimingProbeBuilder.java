package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

/**
 * Builder Interface for the TimingProbe.
 */
public interface TimingProbeBuilder<I extends AbstractInput, O extends AbstractOutput> {

    /**
     * Builds a new TimingProbe instance.
     *
     * @param timingProbeEnabler  the configuration that enables the timing probe testing
     * @return                    a new TimingProbe instance
     */
    TimingProbe<I, O> build(TimingProbeEnabler timingProbeEnabler);
}
