package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AlphabetSerializer for alphabet in XML format.
 *
 * @param <I>   the type of inputs
 * @param <AP>  the type of alphabet pojo
 */
public class AlphabetSerializerXml<I, AP extends AlphabetPojoXml<I>> implements AlphabetSerializer<I> {

    /** Stores the singleton JAXB context. */
    protected JAXBContext context;

    /** Stores the constructor parameter. */
    protected Class<I> inputClass;

    /** Stores the constructor parameter. */
    protected Class<AP> alphabetPojoXmlChildClass;

    /**
     * Returns the {@link #context} or creates and returns a new JAXBContext
     * if {@link #context} is null.
     *
     * @return  the JAXBContext instance stored in {@link #context}
     *
     * @throws JAXBException  if the instance cannot be created
     */
    protected synchronized JAXBContext getJAXBContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(inputClass, alphabetPojoXmlChildClass);
        }
        return context;
    }

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param inputClass                 the class of the alphabet inputs
     * @param alphabetPojoXmlChildClass  the class that specifies the alphabet's XML POJO
     */
    public AlphabetSerializerXml(Class<I> inputClass, Class<AP> alphabetPojoXmlChildClass) {
        this.inputClass = inputClass;
        this.alphabetPojoXmlChildClass = alphabetPojoXmlChildClass;
    }

    @Override
    public Alphabet<I> read(InputStream alphabetStream) throws AlphabetSerializerException {
        try {
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(alphabetStream, StandardCharsets.UTF_8));
            Object unmarshalled = unmarshaller.unmarshal(xsr);

            List<I> inputList = List.of();
            if (unmarshalled instanceof AlphabetPojoXml) {
                inputList = alphabetPojoXmlChildClass.cast(unmarshalled).getInputs();
            }
            return new ListAlphabet<I>(inputList);

        } catch (JAXBException | XMLStreamException e) {
            throw new AlphabetSerializerException("Cannot read the alphabet", e);
        }
    }

    @Override
    public void write(OutputStream alphabetStream, Alphabet<I> alphabet) throws AlphabetSerializerException {
        try {
            Marshaller m = getJAXBContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            AP alphabetPojo = alphabetPojoXmlChildClass.getConstructor(List.class).newInstance(new ArrayList<>(alphabet));
            m.marshal(alphabetPojo, alphabetStream);

        } catch (JAXBException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new AlphabetSerializerException("Cannot write the alphabet", e);
        }
    }

    @Override
    public String getAlphabetFileExtension() {
        return ".xml";
    }
}
