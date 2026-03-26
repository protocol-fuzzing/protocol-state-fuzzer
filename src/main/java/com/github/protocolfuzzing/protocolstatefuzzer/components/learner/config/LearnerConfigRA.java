package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * The standard learning configuration for Register Automata.
 */
public class LearnerConfigRA extends LearnerConfigStandard {

    /**
     * Constructs new instance with default parameters.
     *
     */
    public LearnerConfigRA() {
        super();
        super.learningAlgorithm = LearningAlgorithmName.SLSTAR;
        super.equivalenceAlgorithms = Arrays.asList(EquivalenceAlgorithmName.IO_RANDOM_WALK);
    }

    /**
     * Stores the JCommander Parameter -disableIOMode
     * <p>
     * Whether or not to use IO mode, IOMode is used by default. WARNING: Disabling
     * IO-mode will learn the system as an acceptor, unsupported.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-disableIOMode", description = "Whether or not to use IO mode, IOMode is used by default. WARNING: Disabling IO-mode will learn the system as an acceptor, unsupported.")
    protected Boolean disableIOMode = false;

    /**
     * Stores the JCommander Parameter -probNewDataValue
     * <p>
     * The probability of the Equivalence Oracle choosing a new data value
     * <p>
     * Default value: 0.8.
     */
    @Parameter(names = "-probNewDataValue", description = "The probability of some RA equivalence algorithms to choose a new data value")
    protected Double probNewDataValue = 0.8;

    /**
     * Stores the JCommander Parameter -maxRuns
     * <p>
     * The maximum number of runs for some RA equivalence algorithms
     * <p>
     * Default value: 10000.
     */
    @Parameter(names = "-maxRuns", description = "The maximum number of runs for some RA equivalence algorithms")
    protected Integer maxRuns = 10000;

    /**
     * Stores the JCommander Parameter -maxDepthRA
     * <p>
     * The maximum depth for some RA equivalence algorithms
     * <p>
     * Default value: 10.
     */
    @Parameter(names = "-maxDepthRA", description = "The maximum depth for some RA equivalence algorithms")
    protected Integer maxDepthRA = 10;

    /**
     * Stores the JCommander Parameter -resetRuns
     * <p>
     * Whether or not to reset runs.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-resetRuns", description = "Whether or not to reset runs.")
    protected Boolean resetRuns = false;

    /**
     * Stores the JCommander Parameter -SeedTransitions
     * <p>
     * Whether or not transitions should be seeded. If set to true then the
     * equivalence oracle picks a random starting location in the hypothesis and
     * generates a random trace from that location, otherwise only a random location
     * is generated.
     * Setting this to true can speed up the process of finding counter examples.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-seedTransitions", description = "Whether or not transitions should be seeded.")
    protected Boolean seedTransitions = false;

    /**
     * Stores the JCommander Parameter -drawSymbolsUniformly
     * <p>
     * Whether or not symbols should be drawn uniformly. This affects how the
     * equivalence oracle generates the random trace where false means that the next
     * action is picked at random while true means it is chosen by a weighted
     * random.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-drawSymbolsUniformly", description = "Whether or not symbols should be drawn uniformly.")
    protected Boolean drawSymbolsUniformly = false;

    /**
     * Returns the stored value of {@link #disableIOMode}.
     *
     * @return the stored value of {@link #disableIOMode}
     */
    @Override
    public Boolean getDisableIOMode() {
        return disableIOMode;
    }

    /**
     * Returns the stored value of {@link #disableIOMode}.
     *
     * @return the stored value of {@link #disableIOMode}
     */
    @Override
    public Double getProbNewDataValue() {
        return probNewDataValue;
    }

    /**
     * Returns the stored value of {@link #maxRuns}.
     *
     * @return the stored value of {@link #maxRuns}
     */
    @Override
    public Integer getMaxRuns() {
        return maxRuns;
    }

    /**
     * Returns the stored value of {@link #maxDepthRA}.
     *
     * @return the stored value of {@link #maxDepthRA}
     */
    @Override
    public Integer getMaxDepthRA() {
        return maxDepthRA;
    }

    /**
     * Returns the stored value of {@link #resetRuns}.
     *
     * @return the stored value of {@link #resetRuns}
     */
    @Override
    public Boolean getResetRuns() {
        return resetRuns;
    }

    /**
     * Returns the stored value of {@link #seedTransitions}.
     *
     * @return the stored value of {@link #seedTransitions}
     */
    @Override
    public Boolean getSeedTransitions() {
        return seedTransitions;
    }

    /**
     * Returns the stored value of {@link #drawSymbolsUniformly}.
     *
     * @return the stored value of {@link #drawSymbolsUniformly}
     */
    @Override
    public Boolean getDrawSymbolsUniformly() {
        return drawSymbolsUniformly;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        super.printRunDescriptionSelf(printWriter);
        printWriter.println();
        printWriter.println("### LearnerConfigRA Parameters");
        printRDBooleanParam(printWriter, "-disableIOMode", disableIOMode);
        printRDParam(printWriter, "-probNewDataValue", probNewDataValue);
        printRDParam(printWriter, "-maxRuns", maxRuns);
        printRDParam(printWriter, "-maxDepthRA", maxDepthRA);
        printRDBooleanParam(printWriter, "-resetRuns", resetRuns);
        printRDBooleanParam(printWriter, "-seedTransitions", seedTransitions);
        printRDBooleanParam(printWriter, "-drawSymbolsUniformly", drawSymbolsUniformly);
    }
}
