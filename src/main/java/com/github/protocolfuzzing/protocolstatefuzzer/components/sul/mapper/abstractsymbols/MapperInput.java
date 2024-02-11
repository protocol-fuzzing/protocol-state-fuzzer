package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * TODO
 */
public interface MapperInput<S, I, O, P> {

    /**
     * Returns the name of the input.
     * @return  the name of the input
     */
    String getName();

    /**
     * Returns {@code true} if this input symbol is enabled for execution.
     *
     * @param context  the active execution context
     * @return         {@code true} if this input symbol is enabled for execution
     */
    default boolean isEnabled(ExecutionContext<S, I> context) {
        return true;
    }

    /**
     * TODO
     */
    Long getExtendedWait();

    /**
     * TODO
     */
    void setExtendedWait(Long extendedWait);

    /**
     * Updates the context before sending the input and before calling
     * {@link generateProtocolMessage  generateProtocolMessage}.
     *
     * @param context  the active execution context
     */
    abstract void preSendUpdate(ExecutionContext<S, I> context);

    /**
     * Generates the corresponding concrete symbol (aka protocol message)
     * of the current abstract input symbol, providing this way the
     * functionality of abstract-to-concrete Mapper.
     *
     * @param context  the active execution context
     * @return         the corresponding protocol message
     */
    abstract P generateProtocolMessage(ExecutionContext<S, I> context);

    /**
     * Updates the context after sending the input.
     *
     * @param context  the active execution context
     */
    abstract void postSendUpdate(ExecutionContext<S, I> context);

    /**
     * Updates the context after receiving an output.
     *
     * @param output         the output obtained as the response
     * @param outputChecker  the output checker to check the output if needed
     * @param context        the active execution context
     */
    abstract void postReceiveUpdate(
        O output, OutputChecker<O> outputChecker, ExecutionContext<S, I> context);
}
