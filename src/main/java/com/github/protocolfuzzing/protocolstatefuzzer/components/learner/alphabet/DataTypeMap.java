package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;

import java.util.EnumMap;

/**
 * An enum map asociating @link DataTypes with an enumeration member,
 * and for creating DataValues from these types in a convenient manner
 *
 * @param <T> The enumeration type
 */
// TODO: If we are to do this we probably want to override the existing
// implementation.
@SuppressWarnings("serial")
public class DataTypeMap<T extends Enum<T>> extends EnumMap<T, DataType> {

    /** Constructor, takes an existing enum map provided by the Builder */
    private DataTypeMap(EnumMap<T, DataType> map) {
        super(map);
    }

    /**
     * Create a datavalue from an enumMember and a value
     *
     * @param <V>      the type of the value
     * @param enumName the name of the DataType
     * @param value    the value
     * @return a new @link DataValue with value value
     */
    public <V> DataValue<V> newDataValue(T enumName, V value) {
        return new DataValue<V>(this.get(enumName), value);
    }

    public static class Builder<TT extends Enum<TT>> {

        private EnumMap<TT, DataType> map;

        public Builder(Class<TT> enumClass) {
            map = new EnumMap<TT, DataType>(enumClass);
        }

        public <C> Builder<TT> newDataTypes(TT[] nameEnums, Class<C> baseClass) {
            for (TT nameEnum : nameEnums) {
                DataType dataType = new DataType(nameEnum.name(), baseClass);
                map.put(nameEnum, dataType);
            }
            return this;
        }

        public DataTypeMap<TT> build() {
            return new DataTypeMap<TT>(this.map);
        }
    }
}
