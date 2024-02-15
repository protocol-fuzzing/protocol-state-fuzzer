package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;

/**
 * Builder Interface for the StateFuzzer.
 *
 * @param <M>  the type of machine model
 */
public interface StateFuzzerBuilder<M> {

    /**
     * Builds a new StateFuzzer instance.
     *
     * @param stateFuzzerEnabler  the configuration that enables the state fuzzing
     * @return                    a new StateFuzzer instance
     */
    StateFuzzer<M> build(StateFuzzerEnabler stateFuzzerEnabler);
}
