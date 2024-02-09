package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;

/**
 * Interface that enables the state fuzzing by extending the necessary interfaces.
 */
public interface StateFuzzerEnabler<C> extends RunDescriptionPrinter {

    /**
     * Returns the LearnerConfig.
     *
     * @return  the LearnerConfig
     */
    C getLearnerConfig();

    /**
     * Returns the SulConfig.
     *
     * @return  the SulConfig
     */
    SulConfig getSulConfig();

    /**
     * Returns {@code true} if analysis concerns a client implementation.
     *
     * @return {@code true} if analysis concerns a client implementation
     */
    boolean isFuzzingClient();

    /**
     * Returns the directory specified as argument, in which results should be saved.
     *
     * @return the directory specified as argument, in which results should be saved
     */
    String getOutputDir();
}
