package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import java.io.PrintWriter;

/**
 * Interface regarding the client SUL configuration.
 */
public interface SulClientConfig extends SulConfig {

    /**
     * Returns the time (ms) before starting the client.
     * <p>
     * Default value: 50L.
     *
     * @return  the time (ms) before starting the client
     */
    default Long getClientWait() {
        return 50L;
    }

    /**
     * Returns the target port of the SUL client on which the state fuzzer server
     * should listen.
     * <p>
     * Default value: null.
     *
     * @return  the client SUL port or null
     */
    default Integer getPort() {
        return null;
    }

    /**
     * Sets the target port of the SUL client on which the state fuzzer server
     * should listen.
     * <p>
     * Default: does nothing.
     *
     * @param port  the port number to be set
     */
    default void setPort(Integer port) {
    }

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

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        new SulConfig(){}.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulClientConfig Parameters");
        printWriter.println("Client Wait: " + getClientWait());
        printWriter.println("Port: " + getPort());
    }
}
