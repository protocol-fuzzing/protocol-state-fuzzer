package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;

/**
 * The empty server SUL configuration without any JCommander Parameters.
 */
public class SulServerConfigEmpty extends SulConfigEmpty implements SulServerConfig {

    /**
     * Stores the SUL server address to connect the state fuzzer client with format: {@code ip:port}.
     * <p>
     * Default value: null.
     */
    protected String host = null;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public SulServerConfigEmpty() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulServerConfigEmpty(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        super(mapperConfig, sulAdapterConfig);
    }

    /**
     * It does nothing; left to be extended if needed.
     *
     * @param <MCC>   the type of the mapper connection configuration
     * @param config  the configuration regarding the connection of the Mapper with the SUL process
     */
    @Override
    public <MCC> void applyDelegate(MCC config) {
    }

    /**
     * Returns the stored value of {@link #host}.
     *
     * @return  the stored value of {@link #host}
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of {@link #host}.
     *
     * @param host  the host to be set
     */
    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        super.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("SulServerConfigEmpty Non-Explicit Parameters");
        printWriter.println("Connect to: " + getHost());
    }
}
