package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

/**
 * Interface regarding the client SUL configuration.
 */
public interface SulClientConfig extends SulConfig {

    /**
     * Returns the time (ms) before starting the client.
     *
     * @return  the time (ms) before starting the client
     */
    Long getClientWait();

    /**
     * Returns the target port of the SUL client on which the state fuzzer server
     * should listen.
     *
     * @return  the client SUL port or null
     */
    Integer getPort();

    /**
     * Sets the target port of the SUL client on which the state fuzzer server
     * should listen.
     *
     * @param port  the port number to be set
     */
    void setPort(Integer port);

    /**
     * Returns {@code "client"}.
     *
     * @return  {@code "client"}
     */
    @Override
    default String getFuzzingRole() {
        return "client";
    }

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
