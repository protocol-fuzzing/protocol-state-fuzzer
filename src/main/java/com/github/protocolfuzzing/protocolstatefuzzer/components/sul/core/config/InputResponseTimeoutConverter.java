package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;

/**
 * IStringConverter for InputResponseTimeoutMap.
 */
public class InputResponseTimeoutConverter implements IStringConverter<InputResponseTimeoutMap> {

    /**
     * Converts a String to InputResponseTimeoutMap and uses {@link PropertyResolver#resolve(String)}.
     *
     * @param value  the value to be converted
     * @return       the converted value
     */
    @Override
    public InputResponseTimeoutMap convert(String value) {
        InputResponseTimeoutMap inputResponseTimeout = new InputResponseTimeoutMap();
        String resolvedValue = PropertyResolver.resolve(value);
        String[] inputValuePairs = resolvedValue.split("\\,", -1);

        for (String inputValuePair : inputValuePairs) {
            String[] split = inputValuePair.split("\\:", -1);

            if (split.length != 2) {
                throw new ParameterException(errMessage(resolvedValue));
            }

            try {
                inputResponseTimeout.put(split[0], Long.valueOf(split[1]));
            } catch(Exception e) {
                throw new ParameterException(errMessage(resolvedValue), e);
            }
        }

        return inputResponseTimeout;
    }

    /**
     * Returns an error message containing the provided value.
     *
     * @param value  the value that caused the error
     * @return       the error message
     */
    protected String errMessage(String value) {
        return "Error processing InputResponseTimeoutMap from \"" + value + "\". "
            + "Expected format: \"input1:value1,input2:value2...\"; e.g. \"CLIENT_HELLO:100\" ";
    }
}
