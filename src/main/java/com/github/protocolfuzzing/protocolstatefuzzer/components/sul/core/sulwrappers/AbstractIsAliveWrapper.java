package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.sul.SUL;

/**
 * SUL Wrapper that checks for the liveness of the wrapped sul.
 */
public class AbstractIsAliveWrapper<I, O> implements SUL<I, O> {

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Indicates if the {@link #sul} is found to be alive or not. */
    protected boolean isAlive;

    /** Stores the constructor parameter. */
    protected O socketClosedOutput;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Liveness is tracked only if the output is a subclass of {@link AbstractOutput}.
     *
     * @param sul                 the sul to be wrapped
     * @param socketClosedOutput  the output to be returned when the {@link #sul}
     *                            is found to have terminated.
     */
    public AbstractIsAliveWrapper(SUL<I, O> sul, O socketClosedOutput) {
        this.sul = sul;
        this.socketClosedOutput = socketClosedOutput;
    }

    /**
     * Runs before each test; used for setup.
     */
    @Override
    public void pre() {
        sul.pre();
        isAlive = true;
    }

    /**
     * Runs after each test; used for shutdown.
     */
    @Override
    public void post() {
        sul.post();
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul} and checks its
     * aliveness via the {@link AbstractOutput#isAlive()} of its output.
     *
     * @param in  the input of the test
     * @return    the corresponding output or {@link #socketClosedOutput} in case
     *            the {@link #sul} is observed to have terminated
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I in) {
        if (!isAlive) {
            return socketClosedOutput;
        }

        O out = sul.step(in);

        if (out instanceof AbstractOutput) {
            isAlive = ((AbstractOutput) out).isAlive();
        }

        return out;
    }
}
