package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.sul.SUL;

/**
 * Abstract class used as the SUL Oracle using the {@link Mapper} and the {@link SulAdapter}.
 * <p>
 * Subclasses should initialize {@link Mapper} and {@link SulAdapter} to
 * their own implementations.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public interface AbstractSul<I, O, E> extends SUL<I, O> {

    /**
     * Returns the stored value of {@link SulConfig}.
     *
     * @return  the stored value of {@link SulConfig}
     */
    public abstract SulConfig getSulConfig();

    /**
     * Returns the stored value of {@link CleanupTasks}.
     *
     * @return  the stored value of {@link CleanupTasks}
     */
    public abstract CleanupTasks getCleanupTasks();

    /**
     * Sets the value of {@link DynamicPortProvider}.
     *
     * @param dynamicPortProvider  the dynamic port provider to be set
     */
    public abstract void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider);

    /**
     * Returns the stored value of {@link DynamicPortProvider}.
     *
     * @return  the stored value of {@link DynamicPortProvider}
     */
    public abstract DynamicPortProvider getDynamicPortProvider();

    /**
     * Returns the stored value of {@link Mapper}.
     *
     * @return  the stored value of {@link Mapper}
     */
    public Mapper<I, O, E> getMapper();

    /**
     * Returns the stored value of {@link SulAdapter}.
     *
     * @return  the stored value of {@link SulAdapter}
     */
    public abstract SulAdapter getSulAdapter();
}
