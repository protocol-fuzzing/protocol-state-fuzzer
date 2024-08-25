package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.sul.SUL;

/**
 * Abstract class used as the SUL Oracle using the {@link Mapper} and the {@link SulAdapter}.
 * <p>
 * Subclasses should initialize {@link #mapper} and {@link #sulAdapter} to
 * their own implementations.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public abstract class AbstractSul<S, I, O> implements SUL<I, O> {

    /** Stores the constructor parameter. */
    protected SulConfig sulConfig;

    /** Stores the constructor parameter. */
    protected CleanupTasks cleanupTasks;

    /** Stores the dynamic port provider. */
    protected DynamicPortProvider dynamicPortProvider;

    /** Stores the Mapper instance. */
    protected Mapper<S, I, O> mapper;

    /** Stores the SulAdapter instance. */
    protected SulAdapter sulAdapter;

    /**
     * Constructs a new instance from the given parameters initializing
     * {@link #mapper} and {@link #sulAdapter} to null.
     *
     * @param sulConfig     the configuration of the sul
     * @param cleanupTasks  the cleanup tasks to run in the end
     *
     */
    public AbstractSul(SulConfig sulConfig, CleanupTasks cleanupTasks) {
        this.sulConfig = sulConfig;
        this.cleanupTasks = cleanupTasks;
        // mapper and sulAdapter will be provided in subclasses
        this.mapper = null;
        this.sulAdapter = null;
    }

    /**
     * Returns the stored value of {@link #sulConfig}.
     *
     * @return  the stored value of {@link #sulConfig}
     */
    public SulConfig getSulConfig() {
        return sulConfig;
    }

    /**
     * Returns the stored value of {@link #cleanupTasks}.
     *
     * @return  the stored value of {@link #cleanupTasks}
     */
    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    /**
     * Sets the value of {@link #dynamicPortProvider}.
     *
     * @param dynamicPortProvider  the dynamic port provider to be set
     */
    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
        this.dynamicPortProvider = dynamicPortProvider;
    }

    /**
     * Returns the stored value of {@link #dynamicPortProvider}.
     *
     * @return  the stored value of {@link #dynamicPortProvider}
     */
    public DynamicPortProvider getDynamicPortProvider() {
        return dynamicPortProvider;
    }

    /**
     * Returns the stored value of {@link #mapper}.
     *
     * @return  the stored value of {@link #mapper}
     */
    public Mapper<S, I, O> getMapper() {
        return mapper;
    }

    /**
     * Returns the stored value of {@link #sulAdapter}.
     *
     * @return  the stored value of {@link #sulAdapter}
     */
    public SulAdapter getSulAdapter() {
        return sulAdapter;
    }
}
