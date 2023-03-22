package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import java.io.Serial;

public class RemovalException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public RemovalException(String cause) {
		super(cause);
	}

}
