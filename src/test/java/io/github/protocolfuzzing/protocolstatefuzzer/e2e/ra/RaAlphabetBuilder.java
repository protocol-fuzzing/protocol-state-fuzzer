package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;

import java.util.Arrays;
import java.util.List;

/**
 * Builds the alphabet of RALib symbols from the XML alphabet, as an RA fuzzer does.
 * The symbols are the inputs and outputs, each with the data types of its parameters,
 * named as in the model.
 */
public class RaAlphabetBuilder extends AlphabetBuilderTransformer<RaSymbolXml, ParameterizedSymbol> {

    public RaAlphabetBuilder() {
        super(new AlphabetBuilderStandard<>(serializer()));
    }

    /** Returns the builder of the XML alphabet, which the RA test runner reads. */
    public AlphabetBuilderStandard<RaSymbolXml> getXmlAlphabetBuilder() {
        return alphabetBuilderStandard;
    }

    public static AlphabetSerializerXml<RaSymbolXml, RaAlphabetPojoXml> serializer() {
        return new AlphabetSerializerXml<>(RaSymbolXml.class, RaAlphabetPojoXml.class);
    }

    @Override
    public ParameterizedSymbol toTransformedInput(RaSymbolXml symbol) {
        DataType[] types = symbol.getParamTypes().stream().map(DataType::new).toArray(DataType[]::new);
        return symbol instanceof RaSymbolXml.Output
            ? new OutputSymbol(symbol.getName(), types)
            : new InputSymbol(symbol.getName(), types);
    }

    @Override
    public RaSymbolXml fromTransformedInput(ParameterizedSymbol symbol) {
        List<String> types = Arrays.stream(symbol.getPtypes()).map(DataType::getName).toList();
        return symbol instanceof OutputSymbol
            ? new RaSymbolXml.Output(symbol.getName(), types)
            : new RaSymbolXml.Input(symbol.getName(), types);
    }
}
