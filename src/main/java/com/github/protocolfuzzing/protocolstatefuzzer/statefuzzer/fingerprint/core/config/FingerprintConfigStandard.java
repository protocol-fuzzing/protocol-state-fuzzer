package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * The standard Fingerprint configuration.
 */
public class FingerprintConfigStandard implements FingerprintConfig {

    /**
     * Stores the JCommander Parameter -fingerprint.
     * <p>
     * This is the option that enables fingerprinting and
     * should point to a folder with learned models.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-models", description = "The input models for the fingerprint. "
        + "It should point to a folder with learned models.")
    protected String fingerprintPath = null;

    /**
     * Stores the JCommander Parameter -output.
     * <p>
     * The file to output the fingerprint
     * <p>
     * Default value: adg.dot.
     */
    @Parameter(names = "-output", description = "The file to output the fingerprint")
    protected String outputFilename = "adg.dot";

    /**
     * Stores the singleton instance of the {@link PropertyResolver}.
     */
    @ParametersDelegate
    protected PropertyResolver propertyResolver = PropertyResolver.getInstance();

    /**
     * Constructor
     */
    public FingerprintConfigStandard() {}

    /**
     * Returns the value of {@link #fingerprintPath}.
     *
     * @return the value of {@link #fingerprintPath}
     */
    @Override
    public String getModelsPath() {
        return fingerprintPath;
    }

    /**
     * Returns the value of {@link #outputFilename}.
     *
     * @return the value of {@link #outputFilename}
     */
    @Override
    public String getOutputPath() {
        return outputFilename;
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

}
