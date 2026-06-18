package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * Interface regarding the Fingerprint configuration.
 */

public interface FingerprintConfig extends FingerprintEnabler {

    /**
     * Returns null or the path of a folder containing
     * the models to extract the fingerprint
     * <p>
     * Default value: null.
     *
     * @return null or the path to a folder with DOT models if
     *             additional conformance test should be
     *             performed after finding an initial match *
     */
    default String getModelsPath() {
        return null;
    }

    /**
     * Returns null or the path to output the fingerprint DOT
     * <p>
     * Default value: null.
     *
     * @return null or the path to a dot file
     */
    default String getOutputPath() {
        return null;
    }

    /**
     * Returns the singleton PropertyResolver instance.
     * <p>
     * Default: the singleton instance.
     *
     * @return the singleton PropertyResolver instance
     */
    default PropertyResolver getPropertyResolver() {
        return PropertyResolver.getInstance();
    }

    /**
     * Returns the fingerprintConfig.
     * <p>
     * Default value: the instance itself.
     *
     * @return the FingerprintConfig
     */
    @Override
    default FingerprintConfig getFingerprintConfig() {
        return this;
    }
}
