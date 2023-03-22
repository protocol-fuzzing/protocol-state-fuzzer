package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;

public class RoundLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private long roundLimit;

    public RoundLimitReachedException(long roundLimit) {
        super("Experiment has exceeded the round limit given: " + roundLimit);
        this.roundLimit = roundLimit;
    }

    public long getRoundLimit() {
        return roundLimit;
    }
}
