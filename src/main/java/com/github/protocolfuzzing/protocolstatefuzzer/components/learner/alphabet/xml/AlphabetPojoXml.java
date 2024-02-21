package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml;

import java.util.List;

/**
 * POJO class used for XML (de)serialization by {@link AlphabetSerializerXml}.
 * <p>
 * Example of a subclass with an annotated List member inputs:
 * <pre>
 *  {@code @XmlRootElement}(name = "alphabet")}
 *  {@code @XmlAccessorType}(XmlAccessType.FIELD)}
 *  public class {@code AlphabetPojoXmlImpl<I>} extends {@code AlphabetPojoXml<I>} {
 *
 *      {@code @XmlElements}(value = {
 *         {@code @XmlElement}(type = InputA.class, name = "InputA"),
 *         {@code @XmlElement}(type = InputB.class, name = "InputB")
 *      })
 *      protected {@code List<I>} inputs;
 *
 *      public AlphabetPojoImpl AlphabetPojoXmlImpl() {}
 *
 *      public AlphabetPojoImpl AlphabetPojoXmlImpl({@code List<I>} inputs) {
 *         this.inputs = inputs;
 *      }
 *
 *      public {@code List<I>} getInputs() {
 *         return inputs;
 *      }
 *   }
 * </pre>
 *
 * @param <I>  the type of inputs
 */
public abstract class AlphabetPojoXml<I> {

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
    public AlphabetPojoXml(List<I> inputs) {}

    /**
     * Returns the stored list of inputs.
     * <p>
     * It should be overridden, because this always returns null.
     *
     * @return  the stored list of inputs.
     */
    public List<I> getInputs() {
        return List.of();
    }
}
