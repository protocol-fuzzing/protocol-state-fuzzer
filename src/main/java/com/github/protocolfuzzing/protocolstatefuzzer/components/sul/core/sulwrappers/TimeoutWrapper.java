package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TimeLimitReachedException;
import de.learnlib.sul.SUL;

import java.time.Duration;

/**
 * SUL Wrapper used for setting a limit on the active time of the inner sul.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class TimeoutWrapper<I, O> implements SUL<I, O> {

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the constructor parameter. */
    protected Duration duration;

    /** Stores the time (ms) that this wrapper started. */
    protected long startTime;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul       the inner sul to be wrapped
     * @param duration  the duration of the inner sul allowed to run
     */
    public TimeoutWrapper(SUL<I, O> sul, Duration duration) {
        this.sul = sul;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    /**
     * Runs before each test; used for setup.
     */
    @Override
    public void pre() {
        sul.pre();
    }

    /**
     * Runs after each test; used for shutdown.
     *
     * @throws TimeLimitReachedException  if the allowed duration is exceeded
     */
    @Override
    public void post() {
        sul.post();
        if (System.currentTimeMillis() > duration.toMillis() + startTime) {
            throw new TimeLimitReachedException(duration);
        }
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul}.
     *
     * @param in  the input of the test
     * @return    the corresponding output
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I in) {
        return sul.step(in);
    }

}
