package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Map from names of symbols to instances of symbols.
 *
 * @param <AS>  the type of abstract symbol instance
 */
public class NameToAbstractSymbol<AS extends AbstractSymbol> extends LinkedHashMap<String, AS>{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param abstractSymbolInstances  the collection of abstract symbol instances
     */
    public NameToAbstractSymbol(Collection<AS> abstractSymbolInstances) {
        super();
        abstractSymbolInstances.forEach(asi -> put(asi.getName(), asi));
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of this instance
     */
    @Override
    public String toString() {
        return "Abstract Symbol Mapping: \n" + super.toString();
    }
}
