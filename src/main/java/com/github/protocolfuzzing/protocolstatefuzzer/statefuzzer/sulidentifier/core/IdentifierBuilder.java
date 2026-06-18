package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierEnabler;

/**
 * Builder Interface for the Identifier.
 *
 * @param <M> the type of the model representation returned by the identifier
 */
public interface IdentifierBuilder<M> {
    /**
     * Builds a new SulIdentifier instance.
     *
     * @param  identifierEnabler the configuration that enables the identification
     *
     * @return                   a new SulIdentifier instance
     */
    SulIdentifier<M> build(IdentifierEnabler identifierEnabler);
}
