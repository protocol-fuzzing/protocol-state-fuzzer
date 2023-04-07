package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TimeLimitReachedException;
import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;

import java.time.Duration;

public class TimeoutWrapper<I, O> implements SUL<I, O> {
    protected SUL<I, O> sul;
    protected long startTime;
    protected Duration duration;

    public TimeoutWrapper(SUL<I, O> sul, Duration duration) {
        this.sul = sul;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
        if (System.currentTimeMillis() > duration.toMillis() + startTime) {
            throw new TimeLimitReachedException(duration);
        }
    }

    @Override
    public O step(I in) throws SULException {
        return sul.step(in);
    }

}
