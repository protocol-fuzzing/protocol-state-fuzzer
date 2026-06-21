package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;

/**
 * A configuration class for storing the alphabet filename used in a hypothesis.
 */
public class IdentifierAlphabetStore implements LearnerConfig {
    /** The filename of the alphabet used in the identifier. */
    private final String alphabetFilename;

    /**
     * Constructs a new IdentifierEQAlphabetStore with the given alphabet filename.
     *
     * @param alphabetFilename the filename of the alphabet to be used in the identifier
     */
    public IdentifierAlphabetStore(String alphabetFilename) {
        this.alphabetFilename = alphabetFilename;
    }

    /**
     * Returns the filename of the alphabet used for the hypothesis.
     *
     * @return the filename of the alphabet
     */
    @Override
    public String getAlphabetFilename() {
        return this.alphabetFilename;
    }

}
