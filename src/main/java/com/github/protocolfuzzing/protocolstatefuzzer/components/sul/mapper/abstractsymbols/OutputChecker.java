package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

/**
 * Interface for analyzing and checking output symbols so that
 * checking is decoupled from the formation of the output symbols.
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
     * @return                {@code true} if the output contains the initial client message
     */
    boolean hasInitialClientMessage(O output);

    boolean isTimeout(O output);
    boolean isUnknown(O output);
    boolean isSocketClosed(O output);
    boolean isDisabled(O output);
}
