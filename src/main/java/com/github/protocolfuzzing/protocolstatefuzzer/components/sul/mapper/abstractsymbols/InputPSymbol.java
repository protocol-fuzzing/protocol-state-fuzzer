package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

public abstract class InputPSymbol extends PSymbolInstance implements AbstractInput {

    public InputPSymbol(ParameterizedSymbol baseSymbol, DataValue... pValues) {
        super(baseSymbol, pValues);
    }
}
