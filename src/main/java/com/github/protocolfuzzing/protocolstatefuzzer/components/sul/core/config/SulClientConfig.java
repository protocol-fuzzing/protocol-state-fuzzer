package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;

/**
 * The abstract extension of SulConfig regarding a client SUL.
 */
public abstract class SulClientConfig extends SulConfig {

    /**
     * Stores the JCommander Parameter -clientWait.
     * <p>
     * Time (ms) before starting the client.
     * <p>
     * Default value: 50L.
     */
    @Parameter(names = "-clientWait", description = "Time (ms) before starting the client")
    protected Long clientWait = 50L;

    /**
     * Stores the JCommander Parameter -port.
     * <p>
     * The target port of the SUL client on which the state fuzzer server should listen.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-port", required = true, description = "The target port "
        + "of the SUL client on which the state fuzzer server should listen")
    protected Integer port = null;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public SulClientConfig() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulClientConfig(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        super(mapperConfig, sulAdapterConfig);
    }

    /**
     * Returns the stored value of {@link #clientWait}.
     *
     * @return  the stored value of {@link #clientWait}
     */
    public long getClientWait() {
        return clientWait;
    }

    /**
     * Returns the stored value of {@link #port}.
     *
     * @return  the stored value of {@link #port}
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the value of {@link #port}.
     *
     * @param port  the port number to be set
     */
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public final String getFuzzingRole() {
        return "client";
    }

    @Override
    public final boolean isFuzzingClient() {
        return true;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        super.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulClientConfig Parameters");
        printWriter.println("Client Wait: " + getClientWait());
        printWriter.println("Port: " + getPort());
    }
}
