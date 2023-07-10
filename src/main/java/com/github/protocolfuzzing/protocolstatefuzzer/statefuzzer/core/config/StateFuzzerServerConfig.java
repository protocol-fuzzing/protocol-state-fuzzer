package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

/**
 * Interface regarding the StateFuzzer configuration of a server SUL.
 */
public interface StateFuzzerServerConfig extends StateFuzzerConfig {

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    default boolean isFuzzingClient() {
        return false;
    }
}
