package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetPojoXml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

/**
 * The XML alphabet of register automata, holding both inputs and outputs, e.g.
 * {@code <alphabet><Input name="SREQ" params="token"/><Output name="ACK" params="token"/>...</alphabet>}.
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
public class RaAlphabetPojoXml extends AlphabetPojoXml<RaSymbolXml> {

    @XmlElements(value = {@XmlElement(type = RaSymbolXml.Input.class, name = "Input"), @XmlElement(type = RaSymbolXml.Output.class, name = "Output")
    })
    protected List<RaSymbolXml> symbols;

    public RaAlphabetPojoXml() {}

    public RaAlphabetPojoXml(List<RaSymbolXml> symbols) {
        this.symbols = symbols;
    }

    @Override
    public List<RaSymbolXml> getInputs() {
        return symbols;
    }
}
