package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;

public class NameToAbstractSymbol<AS extends AbstractSymbol> extends LinkedHashMap<String, AS>{

    @Serial
    private static final long serialVersionUID = 1L;

    public NameToAbstractSymbol(Collection<AS> inputs) {
        super();
        inputs.forEach(i -> put(i.getName(), i));
    }

    public AS getInput(String name) {
        return get(name);
    }

    public String toString() {
        return "InputMapping: \n" + super.toString();
    }
}
