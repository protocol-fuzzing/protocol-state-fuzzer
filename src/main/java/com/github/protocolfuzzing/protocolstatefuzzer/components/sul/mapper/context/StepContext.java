package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

/**
 * Context used by {@link ExecutionContextStepped}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class StepContext<I, O> {

    /** Stores the unique index number of this step context. */
    protected int index;

    /** Stores the input associated with this step context. */
    protected I input;

    /** Stores the output associated with the input. */
    protected O output;

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
        this.output = null;
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
    public I getInput() {
        return input;
    }

    /**
     * Sets the value of {@link #input}.
     *
     * @param input  the input to be set
     */
    public void setInput(I input) {
        this.input = input;
    }

    /**
     * Returns the stored value of {@link #output}.
     *
     * @return  the stored value of {@link #output}
     */
    public O getOutput() {
        return output;
    }

    /**
     * Sets the value of {@link #output}.
     *
     * @param output  the output to be set
     */
    public void setOutput(O output) {
        this.output = output;
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
