package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

/**
 * Builder interface for the {@link AbstractSUL}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public interface SULBuilder<I, O, E> {

    /**
     * Builds a new instance of the {@link AbstractSUL}.
     *
     * @param sulConfig     the configuration of the sul
     * @param cleanupTasks  the cleanup tasks to run in the end
     * @return              a new AbstractSUL instance
     */
    AbstractSUL<I, O, E> buildSUL(SULConfig sulConfig, CleanupTasks cleanupTasks);

    /**
     * Builds a new instance of the {@link AbstractSUL} wrapper.
     *
     * @return  a new SULWrapper instance
     */
    SULWrapper<I, O, E> buildWrapper();
}
