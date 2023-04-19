package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for a Mealy Machine DOT file.
 */
public class MealyDotParser {

    /**
     * Parses the contents of a Mealy Machine DOT file.
     *
     * @param <I>          the type of inputs
     * @param <O>          the type of outputs
     * @param <A>          the type of automaton
     * @param creator      the automaton creator
     * @param inputStream  the file input stream
     * @param processor    the input output processor
     * @return             the parsed model instance
     *
     * @throws IOException  in case of an error reading from the inputStream
     */
    public static <I, O, A extends MutableMealyMachine<?, I, ?, O>> InputModelData<I, A>
    parse(AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor<I, O> processor) throws IOException {

        InputModelDeserializer<I, A> parser = DOTParsers.mealy(creator, (map) -> {
            Pair<String, String> ioStringPair = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(map);
            return processor.processMealyInputOutput(ioStringPair.getFirst(), ioStringPair.getSecond());
        });

        return parser.readModel(inputStream);
    }

    /**
     * Interface for processing input, output pairs of a Mealy Machine.
     * @param <I>  the type of inputs
     * @param <O>  the type of outputs
     */
    public interface MealyInputOutputProcessor<I, O> {

        /**
         * Processes an input and an output and pairs them together.
         *
         * @param inputName   the name of the input
         * @param outputName  the name of the output
         * @return            the pair of the input, output
         */
        public Pair<I, O> processMealyInputOutput(String inputName, String outputName);
    }
}
