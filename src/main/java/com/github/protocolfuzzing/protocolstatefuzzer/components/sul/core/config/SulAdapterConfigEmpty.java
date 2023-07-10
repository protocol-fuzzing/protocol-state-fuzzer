package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import java.io.PrintWriter;

/**
 * The empty SulAdapter configuration without any JCommander Parameters.
 */
public class SulAdapterConfigEmpty implements SulAdapterConfig {

    /**
     * Returns {@code null}.
     * @return  null
     */
    @Override
    public Integer getAdapterPort() {
        return null;
    }

    /**
     * Returns {@code null}.
     * @return  null
     */
    @Override
    public String getAdapterAddress() {
        return null;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulAdapterConfigEmpty Non-Explicit Parameters");
        printWriter.println("Adapter Port: " + getAdapterPort());
        printWriter.println("Adapter Address: " + getAdapterAddress());
    }
}
