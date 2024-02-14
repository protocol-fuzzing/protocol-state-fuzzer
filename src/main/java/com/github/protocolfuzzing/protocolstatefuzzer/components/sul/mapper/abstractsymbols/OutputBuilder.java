package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

/**
 * Interface for building output symbols.
 *
 * @param <O>  the type of outputs
 */
public interface OutputBuilder<O> {
    /** Special output symbol to show that no response was received during the waiting time. */
    static final String TIMEOUT = "TIMEOUT";

    /** Special output symbol to show that the response could not be identified. */
    static final String UNKNOWN = "UNKNOWN";

    /** Special output symbol to show that the SUL process has terminated. */
    static final String SOCKET_CLOSED = "SOCKET_CLOSED";

    /** Special output symbol to show that the output is disabled. */
    static final String DISABLED = "DISABLED";

    /**
     * Builds an output symbol given its name.
     *
     * @param name  the name of the output symbol
     * @return      the output symbol
     */
    O buildOutput(String name);

    /**
     * Builds the special output symbol for timeout.
     * <p>
     * The default implementation uses the name of {@link #TIMEOUT}.
     *
     * @return  the special output symbol for timeout
     */
    default O buildTimeout() {
        return buildOutput(TIMEOUT);
    }

    /**
     * Builds the special output symbol for unknown.
     * <p>
     * The default implementation uses the name of {@link #UNKNOWN}.
     *
     * @return  the special output symbol for unknown
     */
    default O buildUnknown() {
        return buildOutput(UNKNOWN);
    }

    /**
     * Builds the special output symbol for socket closed.
     * <p>
     * The default implementation uses the name of {@link #SOCKET_CLOSED}.
     *
     * @return  the special output symbol for socket closed
     */
    default O buildSocketClosed() {
        return buildOutput(SOCKET_CLOSED);
    }

    /**
     * Builds the special output symbol for disabled.
     * <p>
     * The default implementation uses the name of {@link #DISABLED}.
     *
     * @return  the special output symbol for disabled
     */
    default O buildDisabled() {
        return buildOutput(DISABLED);
    }
}
