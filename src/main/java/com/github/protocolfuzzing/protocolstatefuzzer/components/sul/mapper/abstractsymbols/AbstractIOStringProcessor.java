package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.util.Collection;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import net.automatalib.common.util.Pair;

/**
 * Implementation of Mealy Machine input and output pair processor.
 */
public class AbstractIOStringProcessor<I> implements MealyDotParser.MealyInputOutputProcessor<I, AbstractOutput> {

    /** Stores the constructor parameter. */
    protected NameToAbstractSymbol<I> inputDefinitions;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alphabet  the collection of input symbols
     */
    public AbstractIOStringProcessor(Collection<I> alphabet) {
        this.inputDefinitions = new NameToAbstractSymbol<>(alphabet);
    }

    @Override
    public Pair<I, AbstractOutput> processMealyInputOutput(String inputName, String outputName) {
        String inputNameCleaned = inputName.trim();
        I input = inputDefinitions.get(inputNameCleaned);

        if (input == null) {
            throw new RuntimeException("Input " + inputNameCleaned + " could not be found in the given mapping.\n "
                + inputDefinitions.toString());
        }

        // TODO The first replacement should be removed as it is for models with the old output splitter (,)
        String outputNameCleaned = outputName
                                    .replaceAll("\\,", "|")
                                    .replaceAll("WARNING\\|", "WARNING,")
                                    .replaceAll("FATAL\\|", "FATAL,")
                                    .trim();

        AbstractOutput output = new AbstractOutput(outputNameCleaned);

        return Pair.of(input, output);
    }
}
