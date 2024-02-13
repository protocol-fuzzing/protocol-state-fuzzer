package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import net.automatalib.common.util.Pair;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Implementation of Mealy Machine input and output pair processor.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 *
 */
public class MealyIOProcessor<I, O> implements MealyDotParser.MealyInputOutputProcessor<I, O> {

    /** Stores the constructor parameter. */
    protected LinkedHashMap<String, I> inputMap;

    /** Stores the constructor parameter. */
    protected OutputBuilder<O> outputBuilder;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alphabet       the collection of input symbols
     * @param outputBuilder  the builder for the output symbols
     */
    public MealyIOProcessor(Collection<I> alphabet, OutputBuilder<O> outputBuilder) {
        this.inputMap = new LinkedHashMap<>();
        alphabet.forEach(i -> inputMap.put(i.toString(), i));
        this.outputBuilder = outputBuilder;
    }

    @Override
    public Pair<I, O> processMealyInputOutput(String inputName, String outputName) {
        String inputNameCleaned = inputName.trim();
        I input = inputMap.get(inputNameCleaned);

        if (input == null) {
            throw new RuntimeException("Input " + inputNameCleaned + " could not be found in the given alphabet.\n "
                + inputMap.toString());
        }

        String outputNameCleaned = outputName.trim();
        O output = outputBuilder.buildOutput(outputNameCleaned);

        return Pair.of(input, output);
    }
}
