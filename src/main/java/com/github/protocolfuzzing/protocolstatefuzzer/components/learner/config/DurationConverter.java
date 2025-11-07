package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.IStringConverter;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

import java.time.Duration;

/**
 * IStringConverter for Duration.
 */
public class DurationConverter implements IStringConverter<Duration> {

    /**
     * Constructor
     */
    public DurationConverter() { }

    /**
     * Converts a String to Duration and uses {@link PropertyResolver#resolve(String)}.
     *
     * @param value   the value to be converted
     * @return        the converted value
     */
    @Override
    public Duration convert(String value) {
        String resolvedValue = PropertyResolver.resolve(value);
        return Duration.parse(resolvedValue);
    }
}
