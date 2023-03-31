package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.beust.jcommander.IStringConverter;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

import java.time.Duration;

public class DurationConverter implements IStringConverter<Duration> {

    @Override
    public Duration convert(String value) {
        String resolvedValue = PropertyResolver.resolve(value);
        return Duration.parse(resolvedValue);
    }
}
