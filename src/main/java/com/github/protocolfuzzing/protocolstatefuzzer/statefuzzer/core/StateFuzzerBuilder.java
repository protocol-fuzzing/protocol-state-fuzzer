package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;

public interface StateFuzzerBuilder {
    StateFuzzer build(StateFuzzerEnabler stateFuzzerEnabler);
}
