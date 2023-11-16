package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import net.automatalib.word.Word;

import java.io.Serial;

/**
 * Exception used by the {@link CachingSULOracle}.
 * <p>
 * Copied from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>
 * and split into this one and {@link NonDeterminismException}.
 */
public class CacheInconsistencyException extends NonDeterminismException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param input      the input for which non-determinism was observed
     * @param oldOutput  the old output corresponding to the input
     * @param newOutput  the new output corresponding to the input and is different from oldOutput
     */
    public CacheInconsistencyException(Word<?> input, Word<?> oldOutput, Word<?> newOutput) {
        super(input, oldOutput, newOutput);
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param message    the message related to the exception
     * @param input      the input for which non-determinism was observed
     * @param oldOutput  the old output corresponding to the input
     * @param newOutput  the new output corresponding to the input and is different from oldOutput
     */
    public CacheInconsistencyException(String message, Word<?> input, Word<?> oldOutput, Word<?> newOutput) {
        super(message, input, oldOutput, newOutput);
    }

}
