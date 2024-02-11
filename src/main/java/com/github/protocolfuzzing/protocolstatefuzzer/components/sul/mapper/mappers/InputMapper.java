package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * It is responsible for the abstract-to-concrete function of the Mapper and
 * for sending protocol message to the SUL.
 * <p>
 * It performs the following:
 * <ol>
 * <li> Updates the execution context prior to the protocol message generation
 * <li> Generate the protocol message from the input symbol
 * <li> Sends the protocol message to the SUL
 * <li> Updates the execution context after sending the protocol message
 * </ol>
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 */
public abstract class InputMapper<S, I extends MapperInput<S, I, O, P>, O, P> {

    /** Stores the constructor parameter. */
    protected MapperConfig mapperConfig;

    /** Stores the constructor parameter. */
    protected OutputChecker<O> outputChecker;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param mapperConfig   the configuration of the Mapper
     * @param outputChecker  the output checker for checking the output symbols if needed
     */
    public InputMapper(MapperConfig mapperConfig, OutputChecker<O> outputChecker) {
        this.mapperConfig = mapperConfig;
        this.outputChecker = outputChecker;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return  the stored value of {@link #mapperConfig}
     */
    public MapperConfig getMapperConfig(){
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #outputChecker}.
     *
     * @return  the stored value of {@link #outputChecker}
     */
    public OutputChecker<O> getOutputChecker() {
        return outputChecker;
    }

    /**
     * Updates the given context prior and after the sending and
     * generates and sends the protocol message derived from
     * the given input symbol.
     *
     * @param input    the input symbol to be used
     * @param context  the active execution context
     */
    public void sendInput(I input, ExecutionContext<S, I, O> context) {
        input.preSendUpdate(context);
        sendMessage(input.generateProtocolMessage(context), context);
        input.postSendUpdate(context);
    }

    /**
     * Sends the protocol message to the SUL.
     *
     * @param message  the protocol message to be sent
     * @param context  the active execution context holding the protocol state
     */
    protected abstract void sendMessage(P message, ExecutionContext<S, I, O> context);

    /**
     * Enables the update of the context after the response from the SUL and the
     * generated output symbol.
     *
     * @param input    the input symbol converted to protocol message and sent
     * @param output   the output symbol converted from the received protocol message
     * @param context  the active execution context
     */
    public void postReceive(I input, O output, ExecutionContext<S, I, O> context) {
        input.postReceiveUpdate(output, outputChecker, context);
    }
}
