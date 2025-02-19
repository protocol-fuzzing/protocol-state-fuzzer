package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser.MealyInputOutputProcessor;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * It provides methods to build models from corresponding files.
 */
public class ModelFactory {

    /**
     * Builds a Mealy Machine from an alphabet and a DOT file.
     *
     * @param <I>          the type of inputs
     * @param <O>          the type of outputs
     * @param dotFilename  the filename of the DOT file
     * @param processor    the processor for the inputs and outputs
     * @return             the built model after parsing
     *
     * @throws IOException  if an error parsing the DOT file occurs
     */
    public static <I, O> MealyMachine<?, I, ?, O> buildProtocolModel(
        String dotFilename,
        MealyInputOutputProcessor<I, O> processor
    ) throws IOException, FormatException {

        InputModelData<I, CompactMealy<I, O>> result = MealyDotParser.parse(
            new CompactMealy.Creator<I, O>(),
            new FileInputStream(dotFilename),
            processor
        );

        return result.model;
    }
}
