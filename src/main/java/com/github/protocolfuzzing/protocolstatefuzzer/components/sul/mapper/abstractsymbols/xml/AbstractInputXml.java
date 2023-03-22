package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import jakarta.xml.bind.annotation.XmlAttribute;

public abstract class AbstractInputXml extends AbstractInput {
    public AbstractInputXml() {
        super();
    }

    public AbstractInputXml(String name) {
        super(name);
    }

    @XmlAttribute(name = "name", required = true)
    protected String name = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @XmlAttribute(name = "extendedWait")
    protected Long extendedWait;

    @Override
    public Long getExtendedWait() {
        return extendedWait;
    }

    @Override
    public void setExtendedWait(Long extendedWait) {
        this.extendedWait = extendedWait;
    }
}
