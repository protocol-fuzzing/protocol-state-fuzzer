package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config;

import java.io.Serial;

/**
 * Exception used to indicate an error regarding the MapperConnectionConfig.
 */
public class MapperConnectionConfigException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public MapperConnectionConfigException() {
        super();
    }

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param msg  the message related to the exception
     */
    public MapperConnectionConfigException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param msg    the message related to the exception
     * @param cause  the cause of this exception
     */
    public MapperConnectionConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
