package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

/**
 * Interface that incorporates an inner state used for the protocol-specific state
 * and is also capable of enabling and disabling the execution.
 */
public interface ExecutionContext<S, I> {

    /**
     * Returns the current state.
     *
     * @return  the current state.
     */
    S getState();

    /**
     * Disables the execution of this execution context.
     */
    void disableExecution();

    /**
     * Enables the execution of this execution context.
     */
    void enableExecution();

    /**
     * Indicates if this execution context is enabled.
     *
     * @return  {@code true} if this execution context is enabled
     */
    boolean isExecutionEnabled();

    /**
     * Adds the given input to this execution context.
     *
     * @param input  the input symbol to be added
     */
    void setInput(I input);
}
