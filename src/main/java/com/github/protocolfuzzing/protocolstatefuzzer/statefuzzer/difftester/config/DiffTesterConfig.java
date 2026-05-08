package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * Interface for the configuration for differential testing.
 */
public interface DiffTesterConfig {

    /**
     * Returns the path to the first model file (modelA).
     *
     * @return the path to modelA
     */
    String getModelA();

    /**
     * Returns the path to the first model file (modelB).
     *
     * @return the path to modelB
     */
    String getModelB();

    /**
     * Returns the path to the alphabet, shared by modelA and modelB.
     *
     * @return the path to the alphabet
     */
    String getAlphabetFile();

    /**
     * Returns the singleton PropertyResolver instance.
     * <p>
     * Default: the singleton instance.
     *
     * @return the singleton PropertyResolver instance
     */
    default PropertyResolver getPropertyResolver() {
        return PropertyResolver.getInstance();
    }
}
