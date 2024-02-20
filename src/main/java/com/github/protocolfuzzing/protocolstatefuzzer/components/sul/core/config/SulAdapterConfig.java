package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

import java.io.PrintWriter;

/**
 * Interface regarding the configuration of the SulAdapter connected to the launch server
 * used to launch and terminate SUL processes.
 */
public interface SulAdapterConfig extends RunDescriptionPrinter {

    /**
     * Returns the port of the launch server to send commands to.
     * <p>
     * Default value: null.
     *
     * @return  the port of the launch server to send commands to or null
     */
    default Integer getAdapterPort() {
        return null;
    }

    /**
     * Returns the address of the launch server to send commands to.
     * <p>
     * Default value: localhost.
     *
     * @return  the address of the launch server to send commands to or null
     */
    default String getAdapterAddress() {
        return "localhost";
    }

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulAdapterConfig Parameters");
        printWriter.println("Adapter Port: " + getAdapterPort());
        printWriter.println("Adapter Address: " + getAdapterAddress());
    }
}
