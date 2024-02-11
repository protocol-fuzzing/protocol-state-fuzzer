package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

/**
 * Interface that incorporates an inner state used for the protocol-specific state
 * and is also capable of enabling and disabling the execution.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public interface ExecutionContext<S, I, O> {

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
     * @param input  the input to be added
     */
    void setInput(I input);

    /**
     * Adds the given output to this execution context.
     *
     * @param output  the output to be added
     */
    void setOutput(O output);
}
