package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.xml.AbstractInputXml;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AlphabetSerializerXml<AP extends AlphabetPojoXml> implements AlphabetSerializer {
    protected JAXBContext context;
    protected final Class<AP> alphabetPojoXmlChildClass;

    protected synchronized JAXBContext getJAXBContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(alphabetPojoXmlChildClass, AbstractInputXml.class);
        }
        return context;
    }

    public AlphabetSerializerXml(Class<AP> alphabetPojoXmlChildClass) {
        this.alphabetPojoXmlChildClass = alphabetPojoXmlChildClass;
    }

    public Alphabet<AbstractInput> read(InputStream alphabetStream) throws AlphabetSerializerException {
        try {
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(alphabetStream));
            AlphabetPojoXml alphabetPojoXml = (AlphabetPojoXml) unmarshaller.unmarshal(xsr);
            return new ListAlphabet<>(alphabetPojoXml.getInputs());

        } catch (JAXBException | XMLStreamException e) {
            throw new AlphabetSerializerException(e.getMessage());
        }
    }

    public void write(OutputStream alphabetStream, Alphabet<AbstractInput> alphabet) throws AlphabetSerializerException {
        try {
            Marshaller m = getJAXBContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            AP alphabetPojo = alphabetPojoXmlChildClass.getConstructor(List.class).newInstance(new ArrayList<>(alphabet));
            m.marshal(alphabetPojo, alphabetStream);

        } catch (JAXBException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new AlphabetSerializerException(e.getMessage());
        }
    }

    @Override
    public String getAlphabetFileExtension() {
        return ".xml";
    }
}
