package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * The parent class of all input symbols.
 * <p>
 * By extending the {@link AbstractSymbol} it offers the functionality that the Learner needs
 * and also provides variables and methods that the Mapper needs.
 */
public abstract class AbstractInput extends AbstractSymbol {

    /** Stores the additional waiting time for this input symbol's SUL response. */
    protected Long extendedWait;

    /**
     * Constructs a new instance from the default super constructor intended for
     * input symbols.
     */
    public AbstractInput() {
        super(true);
    }

    /**
     * Constructs a new instance from the given parameter using the corresponding
     * super constructor intended for input symbols.
     *
     * @param name  the input symbol name
     */
    public AbstractInput(String name) {
        super(name, true);
    }

    /**
     * Returns the stored value of {@link #extendedWait}.
     *
     * @return  the stored value of {@link #extendedWait}
     */
    public Long getExtendedWait() {
        return extendedWait;
    }

    /**
     * Sets the value of {@link #extendedWait}.
     *
     * @param extendedWait  the additional waiting time to be set
     */
    public void setExtendedWait(Long extendedWait) {
        this.extendedWait = extendedWait;
    }

    /**
     * Returns the preferred mapper for this input, which is different from the default Mapper.
     * <p>
     * If there is no preferred Mapper then null is returned, which means that
     * the default Mapper can be used for this input.
     *
     * @param sulConfig  the configuration of the sul
     * @return           the preferred Mapper or null, in which case the default Mapper can be used
     */
    public Mapper getPreferredMapper(SulConfig sulConfig) {
        return null;
    }

    /**
     * Returns {@code true} if this input symbol is enabled for execution.
     *
     * @param context  the active execution context
     * @return         {@code true} if this input symbol is enabled for execution
     */
    public boolean isEnabled(ExecutionContext context) {
        return true;
    }

    /**
     * Updates the context before sending the input and before calling
     * {@link generateProtocolMessage  generateProtocolMessage}.
     *
     * @param context  the active execution context
     */
    public abstract void preSendUpdate(ExecutionContext context);

    /**
     * Generates the corresponding concrete symbol (aka protocol message)
     * of the current abstract input symbol, providing this way the
     * functionality of abstract-to-concrete Mapper.
     *
     * @param context  the active execution context
     * @return         the corresponding protocol message
     */
    public abstract ProtocolMessage generateProtocolMessage(ExecutionContext context);

    /**
     * Updates the context after sending the input.
     *
     * @param context  the active execution context
     */
    public abstract void postSendUpdate(ExecutionContext context);

    /**
     * Updates the context after receiving an output.
     *
     * @param output                 the output obtained as the response
     * @param abstractOutputChecker  the output checker to check the output if needed
     * @param context                the active execution context
     */
    public abstract void postReceiveUpdate(AbstractOutput output,
        AbstractOutputChecker abstractOutputChecker, ExecutionContext context);

    /**
     * Returns the type of the input.
     * <p>
     * The type of the input should correspond to the type of the message the
     * input generates.
     *
     * @return  the type of the input
     */
    public abstract Enum<?> getInputType();
}
