package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.*;
import net.automatalib.alphabet.ListAlphabet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An alphabet constructed from one or more enumerations
 */
// TODO: findbugs wants this to be serializable,
// which we very well could do, since it is possible to want XML-export,
// but implementing this is going to be complicated and not a priority.
// This type should ideally be parameterized by the enum types of inputs,
// outputs and data types but PSF requiring special symbols makes it so that
// There are two kinds of output enum.
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings
public class EnumAlphabet extends ListAlphabet<ParameterizedSymbol> {

    /**
     * A map from the string representation of enumeration members
     * to the @link ParameterizedSymbol used when constructing the symbol
     */
    private final Map<String, ParameterizedSymbol> symbolMap;
    public final DataTypeMap<?> dataTypeMap;

    /**
     * Private constructor, this class can only be built using the {@link Builder}
     *
     * @param symbols The symbol map with which to construct an instance
     */
    private EnumAlphabet(Map<String, ParameterizedSymbol> symbols, DataTypeMap<?> dataTypeMap) {
        super(new ArrayList<ParameterizedSymbol>(symbols.values()));
        this.symbolMap = symbols;
        this.dataTypeMap = dataTypeMap;
    }

    /**
     * Retrieve the @link ParameterizedSymbol asociated with a particular
     * enum member.
     *
     * @param <T>         enum type
     * @param enum_member the member to be used in retreival
     * @return the asociated symbol
     */
    public <T extends Enum<T>> ParameterizedSymbol getPSymbol(T enum_member) {

        ParameterizedSymbol symbol = symbolMap.get(enum_member.name());
        if (symbol == null) {
            throw new RuntimeException("The symbol " + enum_member.name()
                    + " is not present in the alphabet map, the map may have not been initialised properly.");
        }
        return symbol;
    }

    /**
     * The builder class responsible for creating instances of @link EnumAlphabet
     * <p>
     * The current design allows for updating previously built members,
     * by changing multiple
     *
     * with one or more
     * withInput or
     * withOutput.
     * <p>
     * NOTE:
     * Using withInput and withOutput with
     * the same enum
     * is not
     * intended and will mean that there is
     * either an
     * inputsymbol
     * or outputsymbol present in the final
     * alphabet, and
     */
    public static class Builder {

        /** Stores the symbols as they are being built, allows overwriting */
        private HashMap<String, ParameterizedSymbol> symbolMap = new HashMap<>();
        /** Map storing DataTypes used by symbols */
        private DataTypeMap<?> dataTypeMap;

        /**
         * Constructor, requires a map of data types defined by the protocol
         *
         * @param dataTypeMap map of data types
         */
        public Builder(DataTypeMap<?> dataTypeMap) {
            this.dataTypeMap = dataTypeMap;
        }

        /**
         * Constructs an @link InputSymbol from an enum member,
         * with or without @link DataType s
         *
         * @param <T>        any enum type
         * @param enumMember the name of the symbol to add, as an enum
         * @param dataTypes  zero or more DataTypes asociated with this symbol
         * @return the builder
         */
        public <T extends Enum<T>> Builder withInput(T enumMember, DataType... dataTypes) {
            String name = enumMember.name();
            InputSymbol input = new InputSymbol(name, dataTypes);
            symbolMap.put(name, input);
            return this;
        }

        /**
         * Constructs an @link de.learnlib.ralib.words#OutputSymbol from an enum
         * member,
         * with or without @link DataType s
         *
         * @param <T>        any enum type
         * @param enumMember the name of the symbol to add, as an enum
         * @param dataTypes  zero or more DataTypes asociated with this symbol
         * @return the builder
         */
        public <T extends Enum<T>> Builder withOutput(T enumMember, DataType... dataTypes) {
            String name = enumMember.name();
            OutputSymbol output = new OutputSymbol(name, dataTypes);
            symbolMap.put(name, output);
            return this;
        }

        /**
         * Constructs @link InputSymbol from one or more enum members,
         * without @link DataType
         *
         * @param <T>         any enum type
         * @param enumMembers the names of the symbols to add, as enum members.
         *                    Meant to be used with @link enum#values()
         * @return the builder
         */
        public <T extends Enum<T>> Builder withInputs(T[] enumMembers) {
            for (T e : enumMembers) {
                symbolMap.put(e.name(), new InputSymbol(e.name()));
            }
            return this;
        }

        /**
         * Constructs @link OutputSymbol s from one or more enum members,
         * without @link DataType s
         *
         * @param <T>         any enum type
         * @param enumMembers the names of the symbols to add, as enum members.
         *                    Meant to be used with @link enum#values()
         * @return the builder
         */
        public <T extends Enum<T>> Builder withOutputs(T[] enumMembers) {
            for (T e : enumMembers) {
                symbolMap.put(e.name(), new OutputSymbol(e.name()));
            }
            return this;
        }

        /**
         * Builds an instance of @link EnumAlphabet with the symbols defined by:
         *
         * @return An immutable EnumAlphabet instance
         */
        public EnumAlphabet build() {
            return new EnumAlphabet(symbolMap, dataTypeMap);
        }

    }
}
