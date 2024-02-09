package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

/**
 * Builder Interface for the TimingProbe.
 */
public interface TimingProbeBuilder<S, I extends MapperInput<S, I, O>, O extends MapperOutput<O>> {

    /**
     * Builds a new TimingProbe instance.
     *
     * @param timingProbeEnabler  the configuration that enables the timing probe testing
     * @return                    a new TimingProbe instance
     */
    TimingProbe<S, I, O> build(TimingProbeEnabler timingProbeEnabler);
}
