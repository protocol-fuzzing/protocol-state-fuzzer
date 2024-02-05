package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.ParameterizedAbstractInput;
import de.learnlib.ralib.words.PSymbolInstance;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Extension of the ParameterizedAbstractInput for inputs obtained via an xml file.
 */
public abstract class ParameterizedAbstractInputXml extends ParameterizedAbstractInput {

    /** Replaces the ParameterizedAbstractSymbol's name by adding an XmlAttribute. */
    @XmlAttribute(name = "name", required = true)
    protected String xmlName = null;

    /** Replaces the ParameterizedAbstractInput's extendedWait by adding an XmlAttribute. */
    @XmlAttribute(name = "extendedWait")
    protected Long xmlExtendedWait;

    @XmlAttribute(name = "pSymbol")
    protected PSymbolInstance xmlPSymbol;

    /**
     * Constructs a new instance from the corresponding super constructor and
     * initializes also the {@link #xmlName}.
     *
     * @param name  the parameterized input symbol name
     */
    public ParameterizedAbstractInputXml(PSymbolInstance psi) {
        super(psi);
        this.xmlName = psi.getBaseSymbol().getName();
        this.xmlPSymbol = psi;
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
     * Returns the stored value of {@link #xmlPSymbol}.
     *
     * @return  the stored value of {@link #xmlPSymbol}
     */
    public PSymbolInstance getPSymbolInstance() {
        return xmlPSymbol;
    }

    /**
     * Sets the value of {@link #xmlPSymbol}.
     *
     * @param psi  the parameterized symbol instance to be set
     */
    public void setPSymbolInstance(PSymbolInstance psi) {
        this.xmlPSymbol = psi;
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
