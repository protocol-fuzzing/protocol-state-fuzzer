package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;
import java.time.Duration;

/**
 * Exception used when the specified time limit has been reached.
 */
public class TimeLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The duration that has been exceeded. */
    protected Duration duration;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param duration  the duration that has been exceeded
     */
    public TimeLimitReachedException(Duration duration) {
        super("Experiment has exceeded the duration limit given: " + duration);
        this.duration = duration;
    }

    /**
     * Returns the stored {@link #duration}.
     *
     * @return  the stored {@link #duration}
     */
    public Duration getDuration() {
        return duration;
    }
}
