package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract class for building output symbols.
 *
 * @param <O>  the type of outputs
 */
public abstract class OutputBuilder<O> {
    /** Special output symbol to show that no response was received during the waiting time. */
    public static final String TIMEOUT = "TIMEOUT";

    /** Special output symbol to show that the response could not be identified. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Special output symbol to show that the SUL process has terminated. */
    public static final String SOCKET_CLOSED = "SOCKET_CLOSED";

    /** Special output symbol to show that the output is disabled. */
    public static final String DISABLED = "DISABLED";

    /** Stores the map containing user specific replacements of symbols. */
    protected Map<String, String> userSpecificMap = new LinkedHashMap<>();

    /**
     * Builds the exact output symbol corresponding to the provided name.
     *
     * @param name  the name of the output symbol
     * @return      the output symbol
     */
    public abstract O buildOutputExact(String name);

    /**
     * Builds an output symbol given its name respecting the {@link #userSpecificMap}.
     * <p>
     * If there is a replacement in the {@link #userSpecificMap} for the provided
     * name, then the replacement is used instead.
     * <p>
     * The {@link #userSpecificMap} is mostly used for special symbol replacements,
     * stemming from the user-specified mapper configuration.
     *
     * @param name  the name of the output symbol
     * @return      the output symbol
     */
    public O buildOutput(String name) {
        return buildOutputExact(userSpecificMap.getOrDefault(name, name));
    }

    /**
     * Builds the special output symbol for timeout.
     * <p>
     * The default implementation uses the name of {@link #TIMEOUT}.
     *
     * @return  the special output symbol for timeout
     */
    public O buildTimeout() {
        return buildOutput(TIMEOUT);
    }

    /**
     * Builds the special output symbol for unknown.
     * <p>
     * The default implementation uses the name of {@link #UNKNOWN}.
     *
     * @return  the special output symbol for unknown
     */
    public O buildUnknown() {
        return buildOutput(UNKNOWN);
    }

    /**
     * Builds the special output symbol for socket closed.
     * <p>
     * The default implementation uses the name of {@link #SOCKET_CLOSED}.
     *
     * @return  the special output symbol for socket closed
     */
    public O buildSocketClosed() {
        return buildOutput(SOCKET_CLOSED);
    }

    /**
     * Builds the special output symbol for disabled.
     * <p>
     * The default implementation uses the name of {@link #DISABLED}.
     *
     * @return  the special output symbol for disabled
     */
    public O buildDisabled() {
        return buildOutput(DISABLED);
    }

    /**
     * Returns the stored {@link #userSpecificMap}.
     *
     * @return the stored {@link #userSpecificMap}.
     */
    public Map<String, String> getUserSpecificMap() {
        return this.userSpecificMap;
    }
}
