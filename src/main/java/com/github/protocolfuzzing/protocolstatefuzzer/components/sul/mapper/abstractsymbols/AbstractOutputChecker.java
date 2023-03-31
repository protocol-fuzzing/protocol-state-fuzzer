package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

/**
 * Provides an interface for analyzing outputs so that how the actual strings are formed
 * is decoupled from the checking code.
 */
public interface AbstractOutputChecker {
    boolean hasInitialClientMessage(AbstractOutput abstractOutput);
}
