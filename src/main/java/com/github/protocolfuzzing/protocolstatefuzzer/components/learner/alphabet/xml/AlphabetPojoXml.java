package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;

import java.util.List;

/**
 * POJO class used for .xml de-serialization.
 */
public abstract class AlphabetPojoXml {
    /*
     * To be extended with class annotating header like this:
     *  @XmlRootElement(name = "alphabet")
     *  @XmlAccessorType(XmlAccessType.FIELD)
     *  public class AlphabetPojoXmlExt extends AlphabetPojoXml
     *
     * and variable inputs like this:
     *  @XmlElements(value = {
     *		@XmlElement(type = InputA.class, name = "InputA"),
     *		@XmlElement(type = InputB.class, name = "InputB"),
     *		...
     *	})
     * where InputX.class is the corresponding java class to xml element in alphabet file
     *
     * Example of such class:

     @XmlRootElement(name = "alphabet")
     @XmlAccessorType(XmlAccessType.FIELD)
     public class AlphabetPojoXmlExt extends AlphabetPojoXml {
         @XmlElements(value = {
            @XmlElement(type = InputA.class, name = "InputA"),
            @XmlElement(type = InputB.class, name = "InputB")
         })
         protected List<AbstractInput> inputs;

         public AlphabetPojoExt AlphabetPojoXmlExt() {}

         public AlphabetPojoExt AlphabetPojoXmlExt(List<AbstractInput> inputs) {
            this.inputs = inputs;
         }

         public List<AbstractInput> getInputs() {
            return inputs;
         }
      }

     */

    public AlphabetPojoXml() {}

    public AlphabetPojoXml(List<AbstractInput> inputs) {}

    public List<AbstractInput> getInputs(){
        return null;
    }
}
