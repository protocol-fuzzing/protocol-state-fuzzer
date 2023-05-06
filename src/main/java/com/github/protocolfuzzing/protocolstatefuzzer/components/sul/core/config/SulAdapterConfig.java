package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

import java.io.PrintWriter;
/**
 * The configuration regarding the SulAdapter connected to the launch server
 * used to launch and terminate SUL processes.
 */
public class SulAdapterConfig implements RunDescriptionPrinter {

    /**
     * Stores the JCommander Parameter -adapterPort.
     * <p>
     * The port of the launch server to send commands to.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-adapterPort", required = false, description = "The port of the launch server to send commands to")
    private Integer adapterPort = null;

    /**
     * Stores the JCommander Parameter -adapterAddress.
     * <p>
     * The address of the launch server to send commands to.
     * <p>
     * Default value: localhost.
     */
    @Parameter(names = "-adapterAddress", required = false, description = "The address of the launch server to send commands to")
    private String adapterAddress = "localhost";

    /**
     * Returns the stored value of {@link #adapterPort}.
     *
     * @return  the stored value of {@link #adapterPort}
     */
    public Integer getAdapterPort() {
        return adapterPort;
    }

    /**
     * Returns the stored value of {@link #adapterAddress}.
     *
     * @return  the stored value of {@link #adapterAddress}
     */
    public String getAdapterAddress() {
        return adapterAddress;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulAdapterConfig Parameters");
        printWriter.println("Adapter Port: " + getAdapterPort());
        printWriter.println("Adapter Address: " + getAdapterAddress());
    }
}
