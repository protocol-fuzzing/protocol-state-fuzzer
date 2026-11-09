package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;

import java.util.ArrayList;
import java.util.List;

/**
 * A symbol of the XML alphabet, with its name and the data types of its
 * parameters, e.g. {@code <Input name="SREQ" params="token"/>}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class RaSymbolXml {

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlList
    @XmlAttribute(name = "params")
    protected List<String> paramTypes = new ArrayList<>();

    protected RaSymbolXml() {}

    protected RaSymbolXml(String name, List<String> paramTypes) {
        this.name = name;
        this.paramTypes = new ArrayList<>(paramTypes);
    }

    public String getName() {
        return name;
    }

    public List<String> getParamTypes() {
        return List.copyOf(paramTypes);
    }

    /** An input symbol, {@code <Input .../>}. */
    public static class Input extends RaSymbolXml {

        public Input() {}

        public Input(String name, List<String> paramTypes) {
            super(name, paramTypes);
        }
    }

    /** An output symbol, {@code <Output .../>}. */
    public static class Output extends RaSymbolXml {

        public Output() {}

        public Output(String name, List<String> paramTypes) {
            super(name, paramTypes);
        }
    }
}
