package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Extension of the AbstractInput for inputs obtained via an xml file.
 */
public abstract class AbstractInputXml extends AbstractInput {

    /** Replaces the AbstractSymbol's name by adding an XmlAttribute. */
    @XmlAttribute(name = "name", required = true)
    protected String name = null;

    /** Replaces the AbstractInput's extendedWait by adding an XmlAttribute. */
    @XmlAttribute(name = "extendedWait")
    protected Long extendedWait;

    /**
     * Constructs a new instance from the default super constructor.
     */
    public AbstractInputXml() {
        super();
    }

    /**
     * Constructs a new instance from the corresponding super constructor and
     * initializes also the {@link #name}.
     *
     * @param name  the input symbol name
     */
    public AbstractInputXml(String name) {
        super(name);
        this.name = name;
    }

    /**
     * Returns the stored value of {@link #name}.
     *
     * @return  the stored value of {@link #name}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the value of {@link #name}.
     *
     * @param name  the symbol name to be set
     */
    @Override
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the stored value of {@link #extendedWait}.
     *
     * @return  the stored value of {@link #extendedWait}
     */
    @Override
    public Long getExtendedWait() {
        return extendedWait;
    }

    /**
     * Sets the value of {@link #extendedWait}.
     *
     * @param extendedWait  the additional waiting time to be set
     */
    @Override
    public void setExtendedWait(Long extendedWait) {
        this.extendedWait = extendedWait;
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
