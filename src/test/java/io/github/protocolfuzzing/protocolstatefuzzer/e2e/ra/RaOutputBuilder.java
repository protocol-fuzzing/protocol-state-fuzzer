package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

/**
 * Builds the outputs without data values, such as TIMEOUT.
 */
public class RaOutputBuilder extends OutputBuilder<PSymbolInstance> {

    @Override
    public PSymbolInstance buildOutputExact(String name) {
        return new PSymbolInstance(new OutputSymbol(name));
    }
}
