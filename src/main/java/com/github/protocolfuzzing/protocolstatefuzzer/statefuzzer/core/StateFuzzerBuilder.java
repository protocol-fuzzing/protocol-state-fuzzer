package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;

/**
 * Builder Interface for the StateFuzzer.
 */
public interface StateFuzzerBuilder {

    /**
     * Builds a new StateFuzzer instance.
     *
     * @param stateFuzzerEnabler  the configuration that enables the state fuzzing
     * @return                    a new StateFuzzer instance
     */
    StateFuzzer build(StateFuzzerEnabler stateFuzzerEnabler);
}
