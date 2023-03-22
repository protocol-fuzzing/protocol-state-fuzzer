package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * This is the parent class of all input symbols.
 * Because of its extension of {@link AbstractSymbol} the class offers the functionality needed by the learner,
 * but is also extended with variables and methods needed by the mapper.
 */
public abstract class AbstractInput extends AbstractSymbol {
    public AbstractInput() {
        super(true);
    }

    public AbstractInput(String name) {
        super(name, true);
    }

    protected Long extendedWait;

    public Long getExtendedWait() {
        return extendedWait;
    }

    public void setExtendedWait(Long extendedWait) {
        this.extendedWait = extendedWait;
    }

    /**
     * Returns the preferred mapper for this input, or null, if there isn't one,
     * meaning the input does not require alterations to the typical mapping of the input.
     */
    public Mapper getPreferredMapper(SulConfig sulConfig) {
        return null;
    }

    /**
     * Enables the input for execution.
     */
    public boolean isEnabled(ExecutionContext context) {
        return true;
    }

    /**
     * Updates context before sending the input and before {@link AbstractInput#generateProtocolMessage}
     */
    public abstract void preSendUpdate(ExecutionContext context);

    /**
     * Generates the corresponding concrete symbol (aka protocol message) for the current abstract symbol,
     * providing this way the functionality of abstract-to-concrete mapper.
     */
    public abstract ProtocolMessage generateProtocolMessage(ExecutionContext context);

    /**
     * Updates the context after sending the input.
     */
    public abstract void postSendUpdate(ExecutionContext context);

    /**
     * Updates the context after receiving an output.
     */
    public abstract void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
                                           ExecutionContext context);

    /**
     * The type of the input should correspond to the type of the message the
     * input generates.
     */
    public abstract Enum<?> getInputType();

}
