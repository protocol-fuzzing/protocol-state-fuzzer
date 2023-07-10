package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

/**
 * Interface regarding the server SUL configuration.
 */
public interface SulServerConfig extends SulConfig {

    /**
     * Returns the SUL server address to connect the state fuzzer client with format: {@code ip:port}.
     *
     * @return  the SUL server address to connect the state fuzzer client
     */
    String getHost();

    /**
     * Sets the SUL server address to connect the state fuzzer client with format: {@code ip:port}.
     *
     * @param host  the host to be set in the format {@code ip:port}
     */
    void setHost(String host);

    /**
     * Returns {@code "server"}.
     *
     * @return  {@code "server"}
     */
    @Override
    default String getFuzzingRole() {
        return "server";
    }

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
