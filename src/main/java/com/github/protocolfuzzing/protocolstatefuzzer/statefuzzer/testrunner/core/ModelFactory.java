package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser.MealyInputOutputProcessor;
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
     * @param dotFilename  the filename of the DOT file
     * @return             the built model after parsing
     *
     * @throws IOException  if an error parsing the DOT file occurs
     */
    public static <I, O> MealyMachine<?, I, ?, O> buildProtocolModel(
        String dotFilename,
        MealyInputOutputProcessor<I, O> processor
    ) throws IOException {

        InputModelData<I, CompactMealy<I, O>> result = MealyDotParser.parse(
            new CompactMealy.Creator<I, O>(),
            new FileInputStream(dotFilename),
            processor
        );

        return result.model;
    }
}
