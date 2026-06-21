package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;

/**
 * Builder Interface for the FingerprintExtraction.
 */
public interface FingerprintBuilder {
    /**
     * Builds a new FingerprintExtraction instance.
     *
     * @param  fingerprintEnabler the configuration that enables the fingerprint Extraction
     *
     * @return                    a new FingerprintExtraction instance
     */
    FingerprintExtraction build(FingerprintEnabler fingerprintEnabler);
}
