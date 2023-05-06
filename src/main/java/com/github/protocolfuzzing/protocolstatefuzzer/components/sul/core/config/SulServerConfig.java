package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;

/**
 * The abstract extension of SulConfig regarding a server SUL.
 */
public abstract class SulServerConfig extends SulConfig {

    /**
     * Stores the JCommander Parameter -connect.
     * <p>
     * The SUL server address to connect the state fuzzer client. Format: ip:port.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-connect", required = true, description = "The SUL server "
        + "address to connect the state fuzzer client. Format: ip:port")
    protected String host = null;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public SulServerConfig() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulServerConfig(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        super(mapperConfig, sulAdapterConfig);
    }

    /**
     * Returns the stored value of {@link #host}.
     *
     * @return  the stored value of {@link #host}
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of {@link #host}.
     *
     * @param host  the host to be set
     */
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public final String getFuzzingRole() {
        return "server";
    }

    @Override
    public final boolean isFuzzingClient() {
        return false;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        super.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulServerConfig Parameters");
        printWriter.println("Connect to: " + getHost());
    }
}
