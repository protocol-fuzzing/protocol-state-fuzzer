package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;

public interface CommandListenerEnabler {

    /**
     * Returns the LearnerConfig.
     *
     * @return  the LearnerConfig
     */
    LearnerConfig getLearnerConfig();

    /**
     * Returns the SulConfig.
     *
     * @return  the SulConfig
     */
    SulConfig getSulConfig();

    /**
     * Returns the CommandListenerConfig .
     *
     * @return  the CommandListenerConfig
     */
    CommandListenerConfig getCommandListenerConfig();
}
