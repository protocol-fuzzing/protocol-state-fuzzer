package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;

import java.util.EnumMap;

/**
 * An enum map associating {@link DataType} with an enumeration member,
 * and for creating {@link DataValue} from these types in a convenient manner
 *
 * @param <T> The enumeration type
 */
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
     * @return a new {@link DataValue} with value value
     */
    public <V> DataValue<V> newDataValue(T enumName, V value) {
        return new DataValue<V>(this.get(enumName), value);
    }

    /**
     * A DataTypeMap Builder
     *
     * @param <TT> the type of the enum
     */
    public static class Builder<TT extends Enum<TT>> {

        private EnumMap<TT, DataType> map;

        /**
         * DataTypeMap Builder constructor
         *
         * @param enumClass class of the enum
         */
        public Builder(Class<TT> enumClass) {
            map = new EnumMap<TT, DataType>(enumClass);
        }

        /**
         * Creates an {@link DataType} with a predefined base class
         *
         * @param <C>       the type of the base class
         * @param nameEnums the enum values for which to create a DataType
         * @param baseClass the base class for the data type
         * @return the builder
         */
        public <C> Builder<TT> newDataTypes(TT[] nameEnums, Class<C> baseClass) {
            for (TT nameEnum : nameEnums) {
                DataType dataType = new DataType(nameEnum.name(), baseClass);
                map.put(nameEnum, dataType);
            }
            return this;
        }

        /**
         * Creates an instance of {@link DataTypeMap}
         *
         * @return a DataTypeMap
         */
        public DataTypeMap<TT> build() {
            return new DataTypeMap<TT>(this.map);
        }
    }
}
