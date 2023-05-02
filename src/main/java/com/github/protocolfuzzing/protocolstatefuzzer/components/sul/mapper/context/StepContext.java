package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;

/**
 * Context used by {@link ExecutionContextStepped}.
 */
public class StepContext {

    /** Stores the unique index number of this step context. */
    protected int index;

    /** Stores the input symbol associated with this step context. */
    protected AbstractInput input;

    /** Indicates if this step context is disabled. */
    protected boolean disabled;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param index  the unique identifier of this step context
     */
    public StepContext(int index) {
        disabled = false;
        this.index = index;
        this.input = null;
    }

    /**
     * Returns the stored value of {@link #index}.
     *
     * @return  the stored value of {@link #index}
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the stored value of {@link #input}.
     *
     * @return  the stored value of {@link #input}
     */
    public AbstractInput getInput() {
        return input;
    }

    /**
     * Sets the value of {@link #input}.
     *
     * @param input  the input symbol to be set
     */
    public void setInput(AbstractInput input) {
        this.input = input;
    }

    /**
     * Returns the stored value of {@link #disabled}.
     *
     * @return  the stored value of {@link #disabled}
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Disables this step context, which is reflected in {@link #isDisabled()}.
     */
    public void disable() {
        disabled = true;
    }
}
