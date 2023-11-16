package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractIOStringProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.NameToAbstractSymbol;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.InputModelData;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * It is used to build Mealy Machine models.
 */
public class ModelFactory {

    /**
     * Builds a Mealy Machine from an alphabet and a DOT file.
     *
     * @param alphabet     the alphabet of the model
     * @param dotFilename  the filename of the DOT file
     * @return             the built model after parsing
     *
     * @throws IOException  if an error parsing the DOT file occurs
     */
    public static MealyMachine<?, AbstractInput, ?, AbstractOutput> buildProtocolModel(
        Alphabet<AbstractInput> alphabet,
        String dotFilename
    ) throws IOException {

        InputModelData<AbstractInput, CompactMealy<AbstractInput, AbstractOutput>> result = MealyDotParser.parse(
            new CompactMealy.Creator<>(),
            new FileInputStream(dotFilename),
            new AbstractIOStringProcessor(new NameToAbstractSymbol<>(alphabet))
        );

        return result.model;
    }
}
