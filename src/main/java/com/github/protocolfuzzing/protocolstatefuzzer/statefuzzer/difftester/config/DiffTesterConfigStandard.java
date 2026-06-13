package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * The standard DiffTester configuration.
 */
public class DiffTesterConfigStandard implements DiffTesterConfig {

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
     * Stores the JCommander Parameter -alphabet.
     * <p>
     * The path to the alphabet XML file.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-alphabet", required = true, description = "The path to the alphabet XML file")
    protected String alphabetFilePath = null;

    /**
     * Stores the JCommander Parameter -model-a-name.
     * <p>
     * The custom name for model A.
     * If not provided, defaults to the model's path.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-model-a-name", required = false, description = "The custom name for model A")
    protected String modelAName = null;

    /**
     * Stores the JCommander Parameter -model-b-name.
     * <p>
     * The custom name for model B.
     * If not provided, defaults to the model's path.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-model-b-name", required = false, description = "The custom name for model B")
    protected String modelBName = null;

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
    public String getModelAName() {
        return modelAName != null ? modelAName : modelA;
    }

    @Override
    public String getModelBName() {
        return modelBName != null ? modelBName : modelB;
    }

    private final LearnerConfig learnerConfig = new LearnerConfig() {
        @Override
        public String getAlphabetFilename() {
            return alphabetFilePath;
        }
    };

    @Override
    public LearnerConfig getLearnerConfig() {
        return learnerConfig;
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }
}
