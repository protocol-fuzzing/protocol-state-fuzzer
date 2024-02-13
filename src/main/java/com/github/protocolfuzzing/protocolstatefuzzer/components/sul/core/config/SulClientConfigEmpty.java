package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;

/**
 * The empty client SUL configuration without any JCommander Parameters.
 */
public class SulClientConfigEmpty extends SulConfigEmpty implements SulClientConfig {

    /**
     * Stores the target port of the SUL client on which the state fuzzer
     * server should listen.
     * <p>
     * Default value: null.
     */
    protected Integer port = null;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public SulClientConfigEmpty() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulClientConfigEmpty(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        super(mapperConfig, sulAdapterConfig);
    }

    /**
     * It does nothing; left to be extended if needed.
     *
     * @param <MCC>   the type of mapper connection configuration
     * @param config  the configuration regarding the connection of the Mapper with the SUL process
     */
    @Override
    public <MCC> void applyDelegate(MCC config) {
    }

    /**
     * Returns 50L.
     *
     * @return  50L.
     */
    @Override
    public Long getClientWait() {
        return 50L;
    }

    /**
     * Returns the stored value of {@link #port}.
     *
     * @return  the stored value of {@link #port}
     */
    @Override
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the value of {@link #port}.
     *
     * @param port  the port number to be set
     */
    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        super.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulClientConfigEmpty Non-Explicit Parameters");
        printWriter.println("Client Wait: " + getClientWait());
        printWriter.println("Port: " + getPort());
    }
}
