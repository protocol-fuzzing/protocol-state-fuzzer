package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.ToolPropertyAwareConverterFactory;

public class InputResponseTimeoutConverter implements IStringConverter<InputResponseTimeoutMap> {

	@Override
	public InputResponseTimeoutMap convert(String value) {
		InputResponseTimeoutMap inputResponseTimeout = new InputResponseTimeoutMap();
		String resolvedValue = ToolPropertyAwareConverterFactory.resolve(value);
		String[] inputValuePairs = resolvedValue.split("\\,");

		for (String inputValuePair : inputValuePairs) {
			String[] split = inputValuePair.split("\\:");
			if (split.length != 2) {
				throw new ParameterException(errMessage(resolvedValue));
			} else {
				try {
				inputResponseTimeout.put(split[0], Long.valueOf(split[1]));
				} catch(Exception e) {
					throw new ParameterException(errMessage(resolvedValue), e);
				}
			}
		}

		return inputResponseTimeout;
	}

	protected String errMessage(String value) {
		return String.format("Error processing InputResponseTimeoutMap from \"%s\". "
				+ "Expected format: \"input1:value1,input2:value2...\"; e.g. \"CLIENT_HELLO:100\" ", value);
	}

}
