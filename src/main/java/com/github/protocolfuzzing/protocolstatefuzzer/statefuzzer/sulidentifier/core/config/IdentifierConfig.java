package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;

/**
 * Interface regarding the TestRunner configuration.
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
     * Returns null or the path of a folder with DOT models of SUTs
     * for conformance testing
     * <p>
     * Default value: null.
     *
     * @return null or the path to a folder with DOT models if
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
