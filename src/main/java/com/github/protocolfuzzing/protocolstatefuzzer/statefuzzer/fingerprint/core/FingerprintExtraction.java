package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

/** Interface for Fingerprint extraction */
public interface FingerprintExtraction {

    /**
     * Runs the fingerprinting algorithm and returns a decision tree (Adaptive Distinguish Graph - ADG)
     *
     * @return the root of the ADG as a {@link FingerprintNode}
     */
    public FingerprintNode run() throws RuntimeException;
}
