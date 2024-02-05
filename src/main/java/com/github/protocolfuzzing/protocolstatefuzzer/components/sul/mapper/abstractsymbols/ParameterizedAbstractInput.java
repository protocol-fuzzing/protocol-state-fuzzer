package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

public abstract class ParameterizedAbstractInput extends AbstractInput {
    private ParameterizedSymbol pSymbol;
    private DataValue[] parameterValues;

    public ParameterizedAbstractInput(PSymbolInstance psi) {
        ParameterizedSymbol pSymbol = psi.getBaseSymbol();
        DataValue[] parameterValues = psi.getParameterValues();
        this.name = pSymbol.getName();
        this.pSymbol = pSymbol;
        this.parameterValues = parameterValues;
    }

    public InputSymbol toInputSymbol() {
        return new InputSymbol(this.pSymbol.getName(), this.pSymbol.getPtypes());
    }

    public PSymbolInstance toPSymbolInstance() {
        return new PSymbolInstance(this.toInputSymbol(), parameterValues);
    }

    public ParameterizedSymbol getPSymbol() {
        return this.pSymbol;
    }

    public DataValue[] getPValues() {
        return this.parameterValues;
    }
}
