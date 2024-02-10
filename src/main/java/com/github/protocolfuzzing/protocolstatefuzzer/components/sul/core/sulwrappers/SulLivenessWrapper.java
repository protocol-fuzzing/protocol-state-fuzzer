package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import de.learnlib.sul.SUL;

/**
 * SUL Wrapper that checks for the liveness of the wrapped sul.
 */
public class SulLivenessWrapper<I, O> implements SUL<I, O> {

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Indicates if the {@link #sul} is found to be alive or not. */
    protected boolean isAlive;

    /** Stores the liveness tracker of the sul */
    protected SulLivenessTracker sulLivenessTracker;

    /** Stores the constructor parameter. */
    protected O terminatedOutput;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * Liveness is tracked only if the output is a subclass of {@link MapperOutput}.
     *
     * @param sul                 the sul to be wrapped
     * @param sulLivenessTracker  the liveness tracker of the sul
     * @param terminatedOutput    the output to be returned when the {@link #sul}
     *                            is found to have terminated.
     */
    public SulLivenessWrapper(SUL<I, O> sul, SulLivenessTracker sulLivenessTracker, O terminatedOutput) {
        this.sul = sul;
        this.sulLivenessTracker = sulLivenessTracker;
        this.terminatedOutput = terminatedOutput;
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
     * aliveness via the {@link MapperOutput#isAlive()} of its output.
     *
     * @param input  the input of the test
     * @return       the corresponding output or {@link #terminatedOutput} in case
     *               the {@link #sul} is observed to have terminated
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I input) {
        if (!isAlive) {
            return terminatedOutput;
        }
        O output = sul.step(input);
        isAlive = sulLivenessTracker.isAlive();
        return output;
    }
}
