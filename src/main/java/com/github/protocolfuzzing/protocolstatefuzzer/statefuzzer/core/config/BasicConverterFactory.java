package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * The implementation of JCommander's IStringConverterFactory for the basic
 * classes.
 * <p>
 * An argument is converted to the appropriate class using a designated converter.
 */
public class BasicConverterFactory implements IStringConverterFactory {

    /** The map from classes representing types to converters */
    protected Map<Class<?>, Class<? extends IStringConverter<?>>> converters = new HashMap<>();

    /**
     * Constructs a new BasicConverterFactory adding converters for the
     * String, Integer, Long and Double classes.
     */
    public BasicConverterFactory() {
        converters.put(String.class, StringConverter.class);
        converters.put(Integer.class, IntegerConverter.class);
        converters.put(Long.class, LongConverter.class);
        converters.put(Double.class, DoubleConverter.class);
    }

    /**
     * Returns the corresponding converter to the provided class parameter.
     * <p>
     * This method is used implicitly by JCommander.
     *
     * @param forType  the class destination used as key in {@link #converters}
     * @return         the mapped converter value in {@link #converters} or null
     */
    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class<?> forType) {
        return converters.get(forType);
    }

    /**
     * The converter to String.
     */
    protected static class StringConverter implements IStringConverter<String> {

        /**
         * Converts a String to String and uses the resolve(String) method in {@link PropertyResolver}.
         *
         * @param value  the value to be converted
         * @return       the converted value
         */
        @Override
        public String convert(String value) {
            return PropertyResolver.resolve(value);
        }
    }

    /**
     * The converter to Integer.
     */
    protected static class IntegerConverter implements IStringConverter<Integer> {

        /**
         * Converts a String to Integer and uses the resolve(String) method in {@link PropertyResolver}.
         *
         * @param value  the value to be converted
         * @return       the converted value
         */
        @Override
        public Integer convert(String value) {
            return Integer.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }

    /**
     * The converter to Long.
     */
    protected static class LongConverter implements IStringConverter<Long> {

        /**
         * Converts a String to Long and uses the resolve(String) method in {@link PropertyResolver}.
         *
         * @param value  the value to be converted
         * @return       the converted value
         */
        @Override
        public Long convert(String value) {
            return Long.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }

    /**
     * The converter to Double.
     */
    protected static class DoubleConverter implements IStringConverter<Double> {

        /**
         * Converts a String to Double and uses the resolve(String) method in {@link PropertyResolver}.
         *
         * @param value  the value to be converted
         * @return       the converted value
         */
        @Override
        public Double convert(String value) {
            return Double.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }
}
