package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

/**
 * The base class of both input and output symbols.
 */
public abstract class AbstractSymbol {

    /** The name (abstraction), which should uniquely identify the symbol. */
    protected String name = null;

    /** Indicates if the symbols is an input symbol or not. */
    protected boolean isInput;

    /**
     * Constructs a new instance from the given parameter without a name.
     *
     * @param isInput  {@code true} if the symbol is an input symbol
     */
    public AbstractSymbol(boolean isInput) {
        this.isInput = isInput;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param name     the name of the symbol
     * @param isInput  {@code true} if the symbol is an input symbol
     */
    public AbstractSymbol(String name, boolean isInput) {
        this.name = name;
        this.isInput = isInput;
    }

    /**
     * Returns the stored value of {@link #name}.
     *
     * @return  the stored value of {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of {@link #name}.
     *
     * @param name  the symbol name to be set
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the stored value of {@link #isInput}.
     *
     * @return  the stored value of {@link #isInput}
     */
    public boolean getIsInput() {
        return isInput;
    }

    /**
     * Returns the stored value of {@link #name} with a distinguishing prefix
     * different for an input and an output.
     *
     * @return  the stored value of {@link #name} with a distinguishing prefix
     */
    public String inputDistinguishingName() {
        return (isInput ? "I_" : "O_") + name;
    }

    /**
     * Overrides the default method.
     *
     * @return  the {@link #name} of this symbol
     */
    @Override
    public String toString() {
        return name;
    }
}
