package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;

/**
 * Interface regarding the (SUL) Identifier configuration.
 */
public interface IdentifierConfig extends LearnerConfig {

    /**
     * Returns null or the path of a DOT model of the ADG
     * <p>
     * Default value: null.
     *
     * @return null or the path of a DOT model
     */
    default String getAdgPath() {
        return null;
    }

    /**
     * Returns null or the path of a directory with DOT models of SUTs
     * for conformance testing
     * <p>
     * Default value: null.
     *
     * @return null or the path to a directory with DOT models if
     *             additional conformance test should be
     *             performed after finding an initial match
     */
    default String getConformance() {
        return null;
    }

    /**
     * Returns null or the path to the alphabet required for the ADG
     * <p>
     * Default value: null.
     *
     * @return null or the path of an alphabet file
     */

    @Override
    default String getAlphabetFilename() {
        return null;
    }
}
