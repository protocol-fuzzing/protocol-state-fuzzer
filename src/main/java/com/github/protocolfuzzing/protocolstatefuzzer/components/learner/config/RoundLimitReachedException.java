package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;

/**
 * Exception used when the specified round limit has been reached.
 */
public class RoundLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The number of rounds that has been reached. */
    protected long roundLimit;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param roundLimit  the limit of rounds that has been reached
     */
    public RoundLimitReachedException(long roundLimit) {
        super("Experiment has exceeded the round limit given: " + roundLimit);
        this.roundLimit = roundLimit;
    }

    /**
     * Returns the stored {@link #roundLimit}.
     *
     * @return  the stored {@link #roundLimit}
     */
    public long getRoundLimit() {
        return roundLimit;
    }
}
