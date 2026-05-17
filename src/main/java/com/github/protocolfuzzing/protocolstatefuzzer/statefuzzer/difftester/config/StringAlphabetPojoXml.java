package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetPojoXml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO class used for XML (de-)serialization of a string-based alphabet.
 * <p>
 * The expected XML format is:
 *
 * <pre>
 * {@code
 * <alphabet>
 *     <input>INPUT_NAME</input>
 *     <input>ANOTHER_INPUT</input>
 * </alphabet>
 * }
 * </pre>
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
public class StringAlphabetPojoXml extends AlphabetPojoXml<String> {

    @XmlElement(name = "input")
    private List<String> inputs;

    /**
     * Constructs a new instance with an empty input list.
     */
    public StringAlphabetPojoXml() {
        this.inputs = new ArrayList<>();
    }

    /**
     * Constructs a new instance with the given input list.
     *
     * @param inputs the list of string inputs
     */
    public StringAlphabetPojoXml(List<String> inputs) {
        this.inputs = inputs;
    }

    @Override
    public List<String> getInputs() {
        return inputs;
    }
}
