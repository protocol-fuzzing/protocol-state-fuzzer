package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigProvider;

public interface StateFuzzerEnabler extends LearnerConfigProvider, SulConfigProvider {
    /**
     * @return true if analysis concerns a client implementation, false otherwise
     */
    boolean isFuzzingClient();

    /**
     * @return the output directory specified as argument in which results should be saved
     */
    String getOutputDir();
}
