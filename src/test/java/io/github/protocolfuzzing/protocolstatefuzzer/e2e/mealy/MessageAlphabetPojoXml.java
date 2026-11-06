package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetPojoXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

/**
 * The XML alphabet: {@code <alphabet><Input name="AUTH"/>...</alphabet>}.
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageAlphabetPojoXml extends AlphabetPojoXml<MessageInput> {

    @XmlElements(value = {@XmlElement(type = MessageInput.class, name = "Input")})
    protected List<MessageInput> inputs;

    public MessageAlphabetPojoXml() {}

    public MessageAlphabetPojoXml(List<MessageInput> inputs) {
        this.inputs = inputs;
    }

    @Override
    public List<MessageInput> getInputs() {
        return inputs;
    }

    /**
     * Creates the serializer of the XML alphabets of the Mealy scenarios.
     *
     * @return the serializer
     */
    public static AlphabetSerializerXml<MessageInput, MessageAlphabetPojoXml> serializer() {
        return new AlphabetSerializerXml<>(MessageInput.class, MessageAlphabetPojoXml.class);
    }
}
