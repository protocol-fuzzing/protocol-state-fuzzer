package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import java.io.Serial;

/**
 * Exception used by the {@link ObservationTree}.
 */
public class RemovalException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param message  the message related to the exception
     */
    public RemovalException(String message) {
        super(message);
    }
}
