package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;


/**
 * The standard SulAdapter configuration.
 */
public class SulAdapterConfigStandard implements SulAdapterConfig {

    /**
     * Stores the JCommander Parameter -adapterPort.
     * <p>
     * The port of the launch server to send commands to.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-adapterPort", required = false, description = "The port of the launch server to send commands to")
    protected Integer adapterPort = null;

    /**
     * Stores the JCommander Parameter -adapterAddress.
     * <p>
     * The address of the launch server to send commands to.
     * <p>
     * Default value: localhost.
     */
    @Parameter(names = "-adapterAddress", required = false, description = "The address of the launch server to send commands to")
    protected String adapterAddress = "localhost";

    @Override
    public Integer getAdapterPort() {
        return adapterPort;
    }

    @Override
    public String getAdapterAddress() {
        return adapterAddress;
    }

    /**
     *
     * @param adapterPort the adapterPort to set
     * @param adapterAddress the adapterAddress to set
     */
    public SulAdapterConfigStandard(int adapterPort, String adapterAddress) {
        this.adapterPort = adapterPort;
        this.adapterAddress = adapterAddress;
    }

    /**
     * Constructor
     */
    public SulAdapterConfigStandard() {   }
}
