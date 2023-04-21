package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

/**
 * Exception used by the AlphabetSerializer.
 */
public class AlphabetSerializerException extends Exception {

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param msg  the message related to the exception
     */
    public AlphabetSerializerException(String msg) {
        super(msg);
    }
}
