package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Extension of the AbstractSymbol for inputs obtained via an xml file.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 */
public abstract class AbstractInputXml<S, I, O, P> extends AbstractSymbol implements MapperInput<S, I, O, P> {

    /** Replaces the AbstractSymbol's name by adding an XmlAttribute. */
    @XmlAttribute(name = "name", required = true)
    protected String xmlName = null;

    /** Replaces the AbstractSymbol's extendedWait by adding an XmlAttribute. */
    @XmlAttribute(name = "extendedWait")
    protected Long xmlExtendedWait;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public AbstractInputXml() {
        super(true);
    }

    /**
     * Constructs a new instance from the corresponding super constructor and
     * initializes also the {@link #xmlName}.
     *
     * @param name  the input symbol name
     */
    public AbstractInputXml(String name) {
        super(name, true);
        this.xmlName = name;
    }

    /**
     * Returns the stored value of {@link #xmlName}.
     *
     * @return  the stored value of {@link #xmlName}
     */
    @Override
    public String getName() {
        return xmlName;
    }

    /**
     * Sets the value of {@link #xmlName}.
     *
     * @param name  the symbol name to be set
     */
    @Override
    protected void setName(String name) {
        this.xmlName = name;
    }

    /**
     * Returns the stored value of {@link #xmlExtendedWait}.
     *
     * @return  the stored value of {@link #xmlExtendedWait}
     */
    @Override
    public Long getExtendedWait() {
        return xmlExtendedWait;
    }

    /**
     * Sets the value of {@link #xmlExtendedWait}.
     *
     * @param extendedWait  the additional waiting time to be set
     */
    @Override
    public void setExtendedWait(Long extendedWait) {
        this.xmlExtendedWait = extendedWait;
    }

    /**
     * Overrides the default method.
     *
     * @return  the {@link #xmlName} of this symbol
     */
    @Override
    public String toString() {
        return xmlName;
    }
}
