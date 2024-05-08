package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

/**
 * Special output symbols with special meanings used by PSF.
 * TODO: Check over these with Thanasis
 */
public enum PSFOutputSymbols {
    /** The message is unsupported, TODO: add some more detail */
    UNSUPPORTED_MESSAGE,
    /** The message was not successfully received */
    UNSUCCESSFUL_MESSAGE,
    /** The SUL timed out, no response was received */
    TIMEOUT,
    /** Unknown message recieved */
    UNKNOWN,
    /** The connection has been terminated */
    SOCKET_CLOSED,
    /** Mapper response when a disallowed symbol has been sent to the SUL */
    DISABLED
}
