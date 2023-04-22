package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

/**
 * Interface that provides the alphabet filename, from which
 * an alphabet can be built.
 */
public interface AlphabetProvider {
    /**
     * Returns the filename that contains the alphabet.
     * @return  the filename that contains the alphabet
     */
    String getAlphabetFilename();
}
