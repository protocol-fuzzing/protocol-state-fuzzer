package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.sul.SUL;

/**
 * Abstract class used as the SUL Oracle using the {@link Mapper} and the {@link SULAdapter}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public interface AbstractSUL<I, O, E> extends SUL<I, O> {

    /**
     * Returns the configuration of the SUL.
     *
     * @return  the configuration of the SUL
     */
    SULConfig getSULConfig();

    /**
     * Returns the tasks to be cleaned up.
     *
     * @return  the tasks to be cleaned up
     */
    CleanupTasks getCleanupTasks();

    /**
     * Sets the dynamic port provider.
     *
     * @param dynamicPortProvider  the dynamic port provider to be set
     */
    void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider);

    /**
     * Returns the stored dynamic port provider if any.
     *
     * @return  the stored dynamic port provider
     */
    DynamicPortProvider getDynamicPortProvider();

    /**
     * Returns the stored mapper.
     *
     * @return  the stored mapper
     */
    Mapper<I, O, E> getMapper();

    /**
     * Returns the stored SUL adapter if any.
     *
     * @return  the stored SUL adapter
     */
    SULAdapter getSULAdapter();
}
