package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;

import java.util.List;

/**
 * POJO class used for XML (de)serialization by {@link AlphabetSerializerXml}.
 * <p>
 * Example of a subclass with an annotated List member inputs:
 * <pre>
 *  {@code @XmlRootElement}(name = "alphabet")}
 *  {@code @XmlAccessorType}(XmlAccessType.FIELD)}
 *  public class AlphabetPojoXmlImpl extends AlphabetPojoXml {
 *
 *      {@code @XmlElements}(value = {
 *         {@code @XmlElement}(type = InputA.class, name = "InputA"),
 *         {@code @XmlElement}(type = InputB.class, name = "InputB")
 *      })
 *      protected {@code List<AbstractInput>} inputs;
 *
 *      public AlphabetPojoImpl AlphabetPojoXmlImpl() {}
 *
 *      public AlphabetPojoImpl AlphabetPojoXmlImpl({@code List<AbstractInput>} inputs) {
 *         this.inputs = inputs;
 *      }
 *
 *      public {@code List<AbstractInput>} getInputs() {
 *         return inputs;
 *      }
 *   }
 * </pre>
 */
public abstract class AlphabetPojoXml {

    /**
     * Default Constructor.
     */
    public AlphabetPojoXml() {}

    /**
     * Constructs a new instance from a list of inputs.
     * <p>
     * It should be overridden to store the parameter into a list of inputs.
     *
     * @param inputs  the list of inputs
     */
    public AlphabetPojoXml(List<AbstractInput> inputs) {}

    /**
     * Returns the stored list of inputs.
     * <p>
     * It should be overridden, because this always returns null.
     *
     * @return  the stored list of inputs.
     */
    public List<AbstractInput> getInputs(){
        return null;
    }
}
