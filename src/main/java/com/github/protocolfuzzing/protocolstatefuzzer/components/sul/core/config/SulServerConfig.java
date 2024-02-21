package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import java.io.PrintWriter;

/**
 * Interface regarding the server SUL configuration.
 */
public interface SulServerConfig extends SulConfig {

    /**
     * Returns the SUL server address to connect the state fuzzer client with format: {@code ip:port}.
     * <p>
     * Default value: null.
     *
     * @return  the SUL server address to connect the state fuzzer client
     */
    default String getHost() {
        return null;
    }

    /**
     * Sets the SUL server address to connect the state fuzzer client with format: {@code ip:port}.
     * <p>
     * Default: does nothing.
     *
     * @param host  the host to be set in the format {@code ip:port}
     */
    default void setHost(String host) {
    }

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

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        new SulConfig(){}.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulServerConfigStandard Parameters");
        printWriter.println("Connect to: " + getHost());
    }
}
