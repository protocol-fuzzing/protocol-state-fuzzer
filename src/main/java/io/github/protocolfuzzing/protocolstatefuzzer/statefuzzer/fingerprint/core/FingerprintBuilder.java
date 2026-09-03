package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;

/**
 * Builder Interface for the Fingerprint.
 */
public interface FingerprintBuilder {
    /**
     * Builds a new Fingerprint instance.
     *
     * @param  fingerprintEnabler the configuration that enables the Fingerprint process
     *
     * @return                    a new Fingerprint instance
     */
    Fingerprint build(FingerprintEnabler fingerprintEnabler);
}
