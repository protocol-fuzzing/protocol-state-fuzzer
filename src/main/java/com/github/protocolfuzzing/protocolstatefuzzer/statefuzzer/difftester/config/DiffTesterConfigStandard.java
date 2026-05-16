package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * The standard DiffTester configuration.
 */
public abstract class DiffTesterConfigStandard implements DiffTesterConfig {

    /**
     * Stores the JCommander Parameter -model-a.
     * <p>
     * The path to first model DOT file.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-model-a", required = true, description = "The path to the first model DOT file")
    protected String modelA = null;

    /**
     * Stores the JCommander Parameter -model-b.
     * <p>
     * The path to the second model DOT file.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-model-b", required = true, description = "The path to the second model DOT file")
    protected String modelB = null;

    /**
     * Stores the singleton instance of the {@link PropertyResolver}.
     */
    @ParametersDelegate
    protected PropertyResolver propertyResolver = PropertyResolver.getInstance();

    /**
     * Constructs a new instance with default values for all parameters.
     */
    public DiffTesterConfigStandard() {}

    @Override
    public String getModelA() {
        return modelA;
    }

    @Override
    public String getModelB() {
        return modelB;
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }
}
