package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.util.List;

/**
 * Interface for building outputs.
 */
public interface OutputBuilder<O> {
    /** Special output symbol to show that no response was received during the waiting time. */
    static final String TIMEOUT = "TIMEOUT";

    /** Special output symbol to show that the response could not be identified. */
    static final String UNKNOWN_MESSAGE = "UNKNOWN_MESSAGE";

    /** Special output symbol to show that the SUL process has terminated. */
    static final String SOCKET_CLOSED = "SOCKET_CLOSED";

    /** Special output symbol to show that the output is disabled. */
    static final String DISABLED = "DISABLED";

    /**
     * TODO
     */
    O buildOutput(String name);

    /**
     * TODO
     */
    <PM> O buildOutput(String name, List<PM> messages);

    /**
     * TODO
     */
    O buildTimeout();

    /**
     * TODO
     */
    O buildUnknown();

    /**
     * TODO
     */
    O buildSocketClosed();

    /**
     * TODO
     */
    O buildDisabled();
}
