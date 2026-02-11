package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;


/**
 * The standard client SUL configuration.
 */
public class SULClientConfigStandard extends SULConfigStandard implements SULClientConfig {

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
    public SULClientConfigStandard() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SULAdapter
     */
    public SULClientConfigStandard(MapperConfig mapperConfig, SULAdapterConfig sulAdapterConfig) {
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
     * Returns the stored value of {@link #clientWait}.
     *
     * @return  the stored value of {@link #clientWait}
     */
    @Override
    public Long getClientWait() {
        return clientWait;
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
}
