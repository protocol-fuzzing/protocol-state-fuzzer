package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

public abstract class AbstractSymbol {
    /**
     * The name (abstraction) by which the symbol can be referred.
     * A name uniquely determines a symbol.
     */
    protected String name = null;

    protected boolean isInput;

    public AbstractSymbol(boolean input) {
        this.isInput = input;
    }

    public AbstractSymbol(String name, boolean input) {
        this.name = name;
        this.isInput = input;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean getIsInput() {
        return isInput;
    }

    public String toString() {
        return name;
    }

    public String inputDistinguishingName() {
        return (getIsInput() ? "I_" : "O_") + name;
    }
}
