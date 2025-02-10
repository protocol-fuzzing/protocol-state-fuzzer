package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTInputModelDeserializer;
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
     * @param <S>          the type of states
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
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> InputModelData<I, A>
    parse(AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor<I, O> processor) throws IOException, FormatException {

        DOTInputModelDeserializer<S, I, A> parser = DOTParsers.mealy(creator, (map) -> {
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
         * Processes the symbols of an input and an output and pairs together their symbols.
         *
         * @param inputName   the name of the input
         * @param outputName  the name of the output
         * @return            the pair of the input and output symbols
         */
        Pair<I, O> processMealyInputOutput(String inputName, String outputName);
    }
}
