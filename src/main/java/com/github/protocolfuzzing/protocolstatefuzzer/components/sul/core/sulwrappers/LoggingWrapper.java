package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import de.learnlib.api.SUL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SUL Wrapper used for logging the inputs to and outputs from the inner sul.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class LoggingWrapper<I, O> implements SUL<I, O> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the constructor parameter. */
    protected String logPrefix;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul        the inner sul to be wrapped
     * @param logPrefix  a distinctive prefix before the actual logging message
     */
    public LoggingWrapper(SUL<I,O> sul, String logPrefix) {
        this.sul = sul;
        this.logPrefix = logPrefix == null ? "" : logPrefix;
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
     */
    @Override
    public void post() {
        sul.post();
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul} and logs the
     * inputs and outputs.
     *
     * @param in  the input of the test
     * @return    the corresponding output
     *
     * @throws de.learnlib.api.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I in) {
        LOGGER.info("{}Propagating input symbol {}", logPrefix, in.toString());
        O out = sul.step(in);
        LOGGER.info("{}Propagating output symbol {}", logPrefix, out.toString());
        LOGGER.info("----------------------------------------");
        return out;
    }
}
