package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

/**
 * Interface for analyzing and checking output symbols so that
 * checking is decoupled from the formation of the output symbols.
 *
 * @param <O>  the type of outputs
 */
public interface OutputChecker<O> {

    /**
     * Returns {@code true} if the output contains the initial message that
     * a client implementation would send.
     * <p>
     * It is useful in cases when the SUL client starts the protocol, while
     * the state fuzzer server is waiting for the initial message.
     *
     * @param output  the output to be checked
     * @return        {@code true} if the output contains the initial client message
     */
    boolean hasInitialClientMessage(O output);

    /**
     * Returns {@code true} if the output is the special symbol for timeout.
     *
     * @param output  the output to be checked
     * @return        {@code true} if the output special symbol for timeout
     */
    boolean isTimeout(O output);

    /**
     * Returns {@code true} if the output is the special symbol for unknown.
     *
     * @param output  the output to be checked
     * @return        {@code true} if the output special symbol for unknown
     */
    boolean isUnknown(O output);

    /**
     * Returns {@code true} if the output is the special symbol for socket closed.
     *
     * @param output  the output to be checked
     * @return        {@code true} if the output special symbol for socket closed
     */
    boolean isSocketClosed(O output);

    /**
     * Returns {@code true} if the output is the special symbol for disabled.
     *
     * @param output  the output to be checked
     * @return        {@code true} if the output special symbol for disabled
     */
    boolean isDisabled(O output);
}
