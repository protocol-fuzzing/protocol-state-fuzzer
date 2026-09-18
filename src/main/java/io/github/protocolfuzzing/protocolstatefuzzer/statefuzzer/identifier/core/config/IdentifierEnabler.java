package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;

/**
 * Interface that enables identifying with the IdentifyRunner by extending the necessary interfaces.
 */

public interface IdentifierEnabler {

    /**
     * Returns the LearnerConfig.
     *
     * @return the LearnerConfig
     */
    LearnerConfig getLearnerConfig();

    /**
     * Returns the SULConfig.
     *
     * @return the SULConfig
     */
    SULConfig getSULConfig();

    /**
     * Returns the IdentifierRunnerConfig.
     *
     * @return the IdentifierRunnerConfig
     */
    IdentifierConfig getIdentifierConfig();

    /**
     * Returns the directory, in which results should be saved.
     *
     * @return the directory, in which results should be saved
     */
    String getOutputDir();
}
