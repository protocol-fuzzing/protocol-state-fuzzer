package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

public interface TimingProbeBuilder {
    TimingProbe build(TimingProbeEnabler timingProbeEnabler);
}
