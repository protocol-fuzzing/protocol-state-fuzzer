package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config.IdentifierEnabler;

/**
 * Builder Interface for the Identifier.
 *
 * @param <M> the type of the model representation returned by the identifier
 */
public interface IdentifierBuilder<M> {
    /**
     * Builds a new Identifier instance.
     *
     * @param  identifierEnabler the configuration that enables the identification
     *
     * @return                   a new Identifier instance
     */
    Identifier<M> build(IdentifierEnabler identifierEnabler);
}
