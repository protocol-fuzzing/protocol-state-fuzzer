package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * TODO
 */
public interface MapperInput<S, I, O> {

    /**
     * Returns the name of the input.
     * @return  the name of the input
     */
    String getName();

    /**
     * Returns the preferred mapper for this input, which is different from the default Mapper.
     * <p>
     * If there is no preferred Mapper then null is returned, which means that
     * the default Mapper can be used for this input.
     *
     * @param sulConfig  the configuration of the sul
     * @return           the preferred Mapper or null, in which case the default Mapper can be used
     */
    Mapper<S, I, O> getPreferredMapper(SulConfig sulConfig);

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
    abstract ProtocolMessage generateProtocolMessage(ExecutionContext<S, I> context);

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

    /**
     * Returns the type of the input.
     * <p>
     * The type of the input should correspond to the type of the message the
     * input generates.
     *
     * @return  the type of the input
     */
    abstract Enum<?> getInputType();
}
