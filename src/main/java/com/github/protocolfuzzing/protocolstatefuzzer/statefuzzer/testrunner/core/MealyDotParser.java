package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

import java.io.IOException;
import java.io.InputStream;

public class MealyDotParser {
    public static <I, O, A extends MutableMealyMachine<?, I, ?, O>> InputModelData<I, A> parse(
            AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor<I, O> processor
    ) throws IOException {
        InputModelDeserializer<I, A> parser = DOTParsers.mealy(creator, (map) -> {
            Pair<String, String> ioStringPair = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(map);
            return processor.processMealyInputOutput(ioStringPair.getFirst(), ioStringPair.getSecond());
        });
        return parser.readModel(inputStream);
    }

    public interface MealyInputOutputProcessor<I, O> {
        Pair<I, O> processMealyInputOutput(String inputName, String outputName);
    }
}
