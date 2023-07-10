package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

/**
 * Interface that provides the alphabet filename, from which
 * an alphabet can be built.
 */
public interface AlphabetProvider {
    /**
     * Returns the provided filename that contains the alphabet or null.
     *
     * @return  the provided filename that contains the alphabet or null
     */
    String getAlphabetFilename();
}
