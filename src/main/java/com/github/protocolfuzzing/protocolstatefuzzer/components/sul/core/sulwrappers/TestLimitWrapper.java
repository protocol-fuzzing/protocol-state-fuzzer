package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.TestLimitReachedException;
import de.learnlib.sul.SUL;

/**
 * SUL Wrapper used for setting a limit on the tests directed at the inner sul.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class TestLimitWrapper<I, O> implements SUL<I, O> {

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the constructor parameter. */
    protected long testLimit;

    /** Stores the current number of tests. */
    protected long numTests;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul    the inner sul to be wrapped
     * @param testLimit  the maximum number of tests to be allowed
     */
    public TestLimitWrapper(SUL<I,O> sul, long testLimit) {
        this.sul = sul;
        this.testLimit = testLimit;
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
     * @throws TestLimitReachedException  if {@link #numTests} equals {@link #testLimit}
     */
    @Override
    public void post() {
        sul.post();
        numTests++;
        if (numTests == testLimit) {
            throw new TestLimitReachedException(testLimit);
        }
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul}.
     *
     * @param input  the input of the test
     * @return       the corresponding output
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I input) {
        return sul.step(input);
    }
}
