package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

/**
 * Builder Interface for the StateFuzzerClientConfig and StateFuzzerServerConfig.
 */
public interface StateFuzzerConfigBuilder {

    /**
     * Builds the StateFuzzerClientConfig.
     *
     * @return  the StateFuzzerClientConfig
     */
    StateFuzzerClientConfig buildClientConfig();

    /**
     * Builds the StateFuzzerServerConfig.
     *
     * @return  the StateFuzzerServerConfig
     */
    StateFuzzerServerConfig buildServerConfig();
}
