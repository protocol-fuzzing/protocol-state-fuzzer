package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

/**
 * Special output symbols with special meanings used by PSF.
 */
public enum PSFOutputSymbols {
    /** The message is unsupported */
    UNSUPPORTED_MESSAGE,
    /** The message was not successfully received */
    UNSUCCESSFUL_MESSAGE,
    /** The SUL timed out, no response was received */
    TIMEOUT,
    /** Unknown message received */
    UNKNOWN,
    /** The connection has been terminated */
    SOCKET_CLOSED,
    /** Mapper response when a disallowed symbol has been sent to the SUL */
    DISABLED
}
