package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigProvider;

/**
 * Interface that enables the state fuzzing by extending the necessary interfaces.
 */
public interface StateFuzzerEnabler extends LearnerConfigProvider, SulConfigProvider, RunDescriptionPrinter {
    /**
     * Returns <code>true</code> if analysis concerns a client implementation.
     *
     * @return <code>true</code> if analysis concerns a client implementation
     */
    boolean isFuzzingClient();

    /**
     * Returns the directory specified as argument, in which results should be saved.
     *
     * @return the directory specified as argument, in which results should be saved
     */
    String getOutputDir();
}
