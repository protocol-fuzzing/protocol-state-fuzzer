package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

public class ParameterizedAbstractOutput extends AbstractOutput {
    private ParameterizedSymbol pSymbol;
    private DataValue[] parameterValues;

    public ParameterizedAbstractOutput(PSymbolInstance psi) {
        super(psi.getBaseSymbol().getName());
        this.pSymbol = psi.getBaseSymbol();
        this.parameterValues = psi.getParameterValues();
    }

    public ParameterizedSymbol getPSymbol() {
        return this.pSymbol;
    }

    public DataValue[] getPValues() {
        return this.parameterValues;
    }

    public PSymbolInstance getPSymbolInstance() {
        // TODO: Maybe just save the PSI and use that.
        return new PSymbolInstance(this.pSymbol, this.parameterValues);
    }

    // TODO: Override equals to also compare types
}
