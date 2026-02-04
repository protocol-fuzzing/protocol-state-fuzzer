package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULAdapter;
import de.learnlib.sul.SUL;

/**
 * SUL Wrapper that uses the {@link SULAdapter} in case the SUL processes are
 * launched using a launch server.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class SULAdapterWrapper<I, O> implements SUL<I, O>, DynamicPortProvider {

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the constructor parameter. */
    protected SULAdapter sulAdapter;

    /** Stores the liveness tracker of the SUL. */
    protected SULLivenessTracker sulLivenessTracker;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul                 the SUL to be wrapped
     * @param sulAdapter          the SULAdapter of the launch server
     * @param sulLivenessTracker  the liveness tracker of the SUL
     */
    public SULAdapterWrapper(SUL<I, O> sul, SULAdapter sulAdapter, SULLivenessTracker sulLivenessTracker) {
        this.sul = sul;
        this.sulAdapter = sulAdapter;
        this.sulLivenessTracker = sulLivenessTracker;
    }

    @Override
    public Integer getSULPort() {
        return sulAdapter.getSULPort();
    }

    /**
     * Runs before each test; used for setup.
     * <p>
     * It uses the {@link #sulAdapter} to launch a new SUL process.
     */
    @Override
    public void pre() {
        sulAdapter.connect();

        // the server should always start first
        if (sulAdapter.isClientLauncher()) {
            // the launch server is for clients and should start second
            sul.pre();
            sulAdapter.start();
        } else {
            // the launch server is for servers and should start first
            sulAdapter.start();
            sul.pre();
        }
    }

    /**
     * Runs after each test; used for shutdown.
     * <p>
     * It uses the {@link #sulAdapter} to stop the SUL process.
     */
    @Override
    public void post() {
        sul.post();
        sulAdapter.stop();
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul}.
     * <p>
     * If it observes that the {@link #sulAdapter} has terminated the SUL process
     * then this is reflected in the {@link #sulLivenessTracker}.
     * method.
     *
     * @param input  the input of the test
     * @return       the corresponding output
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I input) {
        O output = sul.step(input);

        if (sulAdapter.checkStopped()) {
            sulLivenessTracker.setAlive(false);
        }

        return output;
    }
}
