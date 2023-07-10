package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;

/**
 * Interface regarding the configuration of the SulAdapter connected to the launch server
 * used to launch and terminate SUL processes.
 */
public interface SulAdapterConfig extends RunDescriptionPrinter {

    /**
     * Returns the port of the launch server to send commands to.
     *
     * @return  the port of the launch server to send commands to or null
     */
    Integer getAdapterPort();

    /**
     * Returns the address of the launch server to send commands to.
     *
     * @return  the address of the launch server to send commands to or null
     */
    String getAdapterAddress();
}
