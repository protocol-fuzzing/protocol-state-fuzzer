package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

/**
 * Exception used by the TimingProbe.
 */
public class ProbeException extends Exception {

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param msg  the message related to the exception
     */
    public ProbeException(String msg) {
        super(msg);
    }
}
