package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core.config;


import com.beust.jcommander.Parameter;

public class CommandListenerConfig {

    @Parameter(names = "-listenerAddress", required = false, description = "Address at which to listen for commands")
    private String listenerAddress;

    @Parameter(names = "-listenerPort", required = false, description = "This option enables command listener functionality."
            + "A server is launched to listen for commands at this port and execute them on the SUT."
            + "No learning is performed")
    private Integer listenerPort;

    @Parameter(names = "-continuous", required = false, description = "Listener does not stop after processing commands from a client. "
            + "Instead it listens for new clients.")
    private boolean continuous;

    @Parameter(names = "-listenerTimeout", required = false, description = "Timeout (miliseconds) on the listener's server socket. "
            + "0 means listener waits indefinitely.")
    private Integer timeout = 30000;

    public CommandListenerConfig() {
        super();
    }

    public String getListenerAddress() {
        return listenerAddress;
    }

    public Integer getListenerPort() {
        return listenerPort;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public Integer getTimeout() {
        return timeout;
    }
}
