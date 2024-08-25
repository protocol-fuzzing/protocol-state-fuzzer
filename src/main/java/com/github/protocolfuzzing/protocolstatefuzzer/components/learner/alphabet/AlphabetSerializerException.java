package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import java.io.Serial;

/**
 * Exception used by the AlphabetSerializer.
 */
public class AlphabetSerializerException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param msg  the message related to the exception
     */
    public AlphabetSerializerException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param msg    the message related to the exception
     * @param cause  the cause related to the exception
     */
    public AlphabetSerializerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
