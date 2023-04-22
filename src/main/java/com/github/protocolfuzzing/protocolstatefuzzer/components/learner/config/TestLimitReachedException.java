package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;

/**
 * Exception used when the specified test limit has been reached.
 */
public class TestLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The number of tests that has been reached. */
    protected long testLimit;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param testLimit  the limit of tests that has been reached
     */
    public TestLimitReachedException(long testLimit) {
        super("Experiment has exceeded the test limit given: " + testLimit);
        this.testLimit = testLimit;
    }

    /**
     * Returns the stored {@link #testLimit}.
     *
     * @return  the stored {@link #testLimit}
     */
    public long getTestLimit() {
        return testLimit;
    }
}
