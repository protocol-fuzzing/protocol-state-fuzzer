package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

/**
 * Interface regarding the StateFuzzer configuration of a client SUL.
 */
public interface StateFuzzerClientConfig extends StateFuzzerConfig {

    /**
     * Returns {@code true}.
     *
     * @return  {@code true}
     */
    @Override
    default boolean isFuzzingClient() {
        return true;
    }
}
