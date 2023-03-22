package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.ToolPropertyAwareConverterFactory;

public class ProtocolVersionConverter implements IStringConverter<ProtocolVersion> {

    @Override
    public ProtocolVersion convert(String value) {
        try {
            String resolvedValue = ToolPropertyAwareConverterFactory.resolve(value);
            return ProtocolVersion.valueOf(resolvedValue);
        } catch (IllegalArgumentException e) {
            throw new ParameterException("Protocol version " + value + " is not supported. Available versions are: "
                    + String.join(", ", ProtocolVersion.names()));
        }
    }
}
