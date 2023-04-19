package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import net.automatalib.commons.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class AbstractIOStringProcessor implements MealyDotParser.MealyInputOutputProcessor<AbstractInput, AbstractOutput> {
    protected Map<String, AbstractInput> cache;
    protected NameToAbstractSymbol<AbstractInput> definitions;

    public AbstractIOStringProcessor(NameToAbstractSymbol<AbstractInput> definitions) {
        cache = new HashMap<>();
        this.definitions = definitions;
    }

    public Pair<AbstractInput, AbstractOutput> processMealyInputOutput(String inputName, String outputName) {
        inputName = inputName.trim();
        if (!cache.containsKey(inputName)) {
            AbstractInput AbstractInput = definitions.getInput(inputName);
            if (AbstractInput == null) {
                throw new RuntimeException("Input " + inputName
                        + " could not be found in the given mapping.\n "
                        + definitions.toString());
            }
            cache.put(inputName, definitions.getInput(inputName));
        }
        AbstractInput input = cache.get(inputName);
        // FIXME Patchwork to work with models using the old output splitter (,). Should be removed.
        outputName = outputName.replaceAll("\\,", "|");
        outputName = outputName.replaceAll("WARNING\\|", "WARNING,");
        outputName = outputName.replaceAll("FATAL\\|", "FATAL,");

        AbstractOutput output = new AbstractOutput(outputName.trim());

        return Pair.of(input, output);
    }
}
