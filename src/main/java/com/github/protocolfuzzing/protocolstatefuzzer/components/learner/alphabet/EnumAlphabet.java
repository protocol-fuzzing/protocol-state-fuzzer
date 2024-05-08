package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.*;
import net.automatalib.alphabet.ListAlphabet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnumAlphabet extends ListAlphabet<ParameterizedSymbol> {

    private Map<String, ParameterizedSymbol> map;

    private EnumAlphabet(Map<String, ParameterizedSymbol> symbols) {
        super(new ArrayList<ParameterizedSymbol>(symbols.values()));
        this.map = symbols;
    }

    public <T extends Enum<T>> ParameterizedSymbol getPSymbol(T enum_member) {

        ParameterizedSymbol symbol = map.get(enum_member.name());
        if (symbol == null) {
            throw new RuntimeException("The symbol " + symbol
                    + " is not present in the alphabet map, the map may have not been initialised properly.");
        }
        return symbol;
    }

    public static class Builder {

        private HashMap<String, ParameterizedSymbol> map = new HashMap<>();

        public Builder() {
        };

        public <T extends Enum<T>> Builder withInput(T enum_member, DataType... dataTypes) {
            String name = enum_member.name();
            InputSymbol input = new InputSymbol(name, dataTypes);
            map.put(name, input);
            return this;
        }

        public <T extends Enum<T>> Builder withOutput(T enum_member, DataType... dataTypes) {
            String name = enum_member.name();
            OutputSymbol output = new OutputSymbol(name, dataTypes);
            map.put(name, output);
            return this;
        }

        public <T extends Enum<T>> Builder withInputs(T[] enum_members) {
            for (T e : enum_members) {
                map.put(e.name(), new InputSymbol(e.name()));
            }
            return this;
        }

        public <T extends Enum<T>> Builder withOutputs(T[] enum_members) {
            for (T e : enum_members) {
                map.put(e.name(), new OutputSymbol(e.name()));
            }
            return this;
        }

        public EnumAlphabet build() {
            return new EnumAlphabet(map);
        }

    }
}
