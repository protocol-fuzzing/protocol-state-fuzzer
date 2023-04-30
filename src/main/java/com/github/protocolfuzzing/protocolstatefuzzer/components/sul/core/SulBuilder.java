package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

/**
 * Builder interface for the {@link AbstractSul}.
 */
public interface SulBuilder {

    /**
     * Builds a new instance of the {@link AbstractSul}.
     *
     * @param sulConfig     the configuration of the sul
     * @param cleanupTasks  the cleanup tasks to run in the end
     * @return              a new AbstractSul instance
     */
    AbstractSul build(SulConfig sulConfig, CleanupTasks cleanupTasks);
}
