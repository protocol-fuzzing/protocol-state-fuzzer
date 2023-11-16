package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import net.automatalib.common.util.Pair;

/**
 * Implementation of Mealy Machine input and output pair processor.
 */
public class AbstractIOStringProcessor implements MealyDotParser.MealyInputOutputProcessor<AbstractInput, AbstractOutput> {

    /** Stores the constructor parameter. */
    protected NameToAbstractSymbol<AbstractInput> inputDefinitions;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param definitions  the input symbol mapping from names to symbols
     */
    public AbstractIOStringProcessor(NameToAbstractSymbol<AbstractInput> definitions) {
        this.inputDefinitions = definitions;
    }

    @Override
    public Pair<AbstractInput, AbstractOutput> processMealyInputOutput(String inputName, String outputName) {
        String inputNameCleaned = inputName.trim();
        AbstractInput input = inputDefinitions.get(inputNameCleaned);

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
