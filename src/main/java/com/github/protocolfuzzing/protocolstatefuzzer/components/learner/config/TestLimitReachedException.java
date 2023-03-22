package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;

public class TestLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private long testLimit;

    public TestLimitReachedException(long testLimit) {
        super("Experiment has exceeded the test limit given: " + testLimit);
        this.testLimit = testLimit;
    }

    public long getTestLimit() {
        return testLimit;
    }
}
