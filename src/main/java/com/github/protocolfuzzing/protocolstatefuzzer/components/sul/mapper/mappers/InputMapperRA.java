package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

/**
 * It is responsible for the abstract-to-concrete function of the Mapper and
 * for sending protocol message to the SUL.
 * <p>
 * It performs the following:
 * <ol>
 * <li>Updates the execution context prior to the protocol message generation
 * <li>Generate the protocol message from the input symbol
 * <li>Sends the protocol message to the SUL
 * <li>Updates the execution context after sending the protocol message
 * </ol>
 *
 * @param <D> the type of the domain (input and output)
 * @param <P> the type of protocol messages
 * @param <E> the type of execution context
 */
public abstract class InputMapperRA<D, P, E> {

    /** Stores the constructor parameter. */
    protected MapperConfig mapperConfig;

    /** Stores the constructor parameter. */
    protected OutputChecker<D> outputChecker;

    /**
     * TODO: Not great design, but there are no good options.
     * The use of PSymbolInstance means it is more complicated to attach behaviour
     * to the inputs and outputs rather than have this implemented here.
     *
     * Returns {@code true} if this input symbol is enabled for execution.
     *
     * @param input   the input to check if it is enabled or not
     * @param context the active execution context
     * @return {@code true} if this input symbol is enabled for execution
     */
    public boolean isEnabled(D input, E context) {
        return true;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param mapperConfig  the configuration of the Mapper
     * @param outputChecker the output checker for checking the output symbols if
     *                      needed
     */
    public InputMapperRA(MapperConfig mapperConfig, OutputChecker<D> outputChecker) {
        this.mapperConfig = mapperConfig;
        this.outputChecker = outputChecker;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return the stored value of {@link #mapperConfig}
     */
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #outputChecker}.
     *
     * @return the stored value of {@link #outputChecker}
     */
    public OutputChecker<D> getOutputChecker() {
        return outputChecker;
    }

    /**
     * Updates the given context prior and after the sending and
     * generates and sends the protocol message derived from
     * the given input symbol.
     *
     * @param input   the input symbol to be used
     * @param context the active execution context
     */
    public void sendInput(D input, E context) {
        preSendUpdate(input, context);
        sendMessage(generateProtocolMessage(input, context), context);
        postSendUpdate(input, context);
    }

    /**
     * Sends the protocol message to the SUL.
     *
     * @param message the protocol message to be sent
     * @param context the active execution context holding the protocol state
     */
    public abstract void sendMessage(P message, E context);

    /**
     * Enables the update of the context after the response from the SUL and the
     * generated output symbol.
     *
     * @param input   the input symbol converted to protocol message and sent
     * @param output  the output symbol converted from the received protocol message
     * @param context the active execution context
     */
    public void postReceive(D input, D output, E context) {
        postReceiveUpdate(input, output, outputChecker, context);
    }

    /**
     * Updates the context before sending the input and before calling
     * {@link generateProtocolMessage generateProtocolMessage}.
     *
     * @param input   the input symbol converted to protocol message and sent
     * @param context the active execution context
     *
     */
    public abstract void preSendUpdate(D input, E context);

    /**
     * Generates the corresponding concrete symbol (aka protocol message)
     * of the current abstract input symbol, providing this way the
     * functionality of abstract-to-concrete Mapper.
     *
     * @param input   the input symbol converted to protocol message and sent
     * @param context the active execution context
     * @return the corresponding protocol message
     */
    public abstract P generateProtocolMessage(D input, E context);

    /**
     * Updates the context after receiving an output.
     *
     * @param input         the input symbol converted to protocol message and sent
     * @param output        the output obtained as the response
     * @param outputChecker the output checker to check the output if needed
     * @param context       the active execution context
     */
    public abstract void postReceiveUpdate(D input, D output, OutputChecker<D> outputChecker, E context);

    /**
     * Updates the context after sending the input.
     *
     * @param input   the input symbol converted to protocol message and sent
     * @param context the active execution context
     */
    public abstract void postSendUpdate(D input, E context);
}
