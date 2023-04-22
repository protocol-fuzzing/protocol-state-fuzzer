package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import java.io.Serial;

/**
 * Exception used by the TimingProbe.
 */
public class ProbeException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param msg  the message related to the exception
     */
    public ProbeException(String msg) {
        super(msg);
    }
}
