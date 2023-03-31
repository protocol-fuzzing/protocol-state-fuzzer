package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import java.io.Serial;
import java.time.Duration;

public class TimeLimitReachedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Duration duration;

    public TimeLimitReachedException(Duration duration) {
        super("Experiment has exceeded the duration limit given: " + duration);
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
