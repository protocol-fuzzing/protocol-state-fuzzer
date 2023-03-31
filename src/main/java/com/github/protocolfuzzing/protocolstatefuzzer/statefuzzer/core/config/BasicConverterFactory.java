package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

import java.util.HashMap;
import java.util.Map;


public class BasicConverterFactory implements IStringConverterFactory {

    protected Map<Class<?>, Class<? extends IStringConverter<?>>> converters = new HashMap<>();

    public BasicConverterFactory() {
        converters.put(String.class, FromStringConverter.class);
        converters.put(Integer.class, FromIntegerConverter.class);
        converters.put(Long.class, FromLongConverter.class);
        converters.put(Double.class, FromDoubleConverter.class);
    }

    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class<?> forType) {
        return converters.get(forType);
    }

    protected static class FromStringConverter implements IStringConverter<String> {
        @Override
        public String convert(String value) {
            return PropertyResolver.resolve(value);
        }
    }

    protected static class FromIntegerConverter implements IStringConverter<Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }

    protected static class FromLongConverter implements IStringConverter<Long> {
        @Override
        public Long convert(String value) {
            return Long.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }

    protected static class FromDoubleConverter implements IStringConverter<Double> {
        @Override
        public Double convert(String value) {
            return Double.valueOf(PropertyResolver.resolve(value.trim()));
        }
    }
}
