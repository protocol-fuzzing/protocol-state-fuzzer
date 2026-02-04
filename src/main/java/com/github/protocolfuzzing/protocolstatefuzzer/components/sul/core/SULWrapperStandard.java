package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.LoggingWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SULAdapterWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SULLivenessTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SULLivenessWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SULProcessWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.TestLimitWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.TimeoutWrapper;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.sul.CounterSUL;
import de.learnlib.sul.SUL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * The standard implementation of {@link SULWrapper} using wrappers from the package
 * {@link com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers  sulwrappers}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public class SULWrapperStandard<I, O, E> implements SULWrapper<I, O, E> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The updated wrapped sul to be finally returned using {@link #getWrappedSUL()}. */
    protected SUL<I, O> wrappedSUL;

    /** The input counter used for the underlying abstract SUL. */
    protected Counter inputCounter;

    /** The test counter used for the underlying abstract SUL. */
    protected Counter testCounter;

    /** The time limit to be set only once using {@link #setTimeLimit(Duration)}. */
    protected Duration timeLimit;

    /** The test limit to be set only once using {@link #setTestLimit(Long)}. */
    protected Long testLimit;

    /**
     * Constructor
     */
    public SULWrapperStandard() { }

    @Override
    public SULWrapper<I, O, E> wrap(AbstractSUL<I, O, E> abstractSUL) {
        wrappedSUL = abstractSUL;
        SULConfig sulConfig = abstractSUL.getSULConfig();
        SULLivenessTracker sulLivenessTracker = new SULLivenessTracker(true);

        if (sulConfig.getCommand() != null) {
            wrappedSUL = new SULProcessWrapper<>(wrappedSUL, sulConfig, sulLivenessTracker);
        }

        if (sulConfig.getSULAdapterConfig().getAdapterPort() != null) {
            if (abstractSUL.getSULAdapter() == null) {
                throw new RuntimeException("Provided adapter port with a null SULAdapter in AbstractSUL.");
            }

            wrappedSUL = new SULAdapterWrapper<>(wrappedSUL, abstractSUL.getSULAdapter(), sulLivenessTracker);
            abstractSUL.setDynamicPortProvider((DynamicPortProvider) wrappedSUL);
        }

        O terminatedOutput = abstractSUL.getMapper().getOutputBuilder().buildSocketClosed();
        wrappedSUL = new SULLivenessWrapper<>(wrappedSUL, sulLivenessTracker, terminatedOutput);

        wrappedSUL = new CounterSUL<>(wrappedSUL);
        inputCounter = CounterSUL.class.cast(wrappedSUL).getSymbolCounter();
        testCounter = CounterSUL.class.cast(wrappedSUL).getResetCounter();

        return this;
    }

    @Override
    public SULWrapper<I, O, E> setTimeLimit(Duration timeLimit) {
        if (timeLimit == null || timeLimit.isNegative() || timeLimit.isZero()) {
            LOGGER.info("Learning time limit NOT set (provided value: {})", timeLimit);
        } else if (this.timeLimit == null) {
            this.timeLimit = timeLimit;
            wrappedSUL = new TimeoutWrapper<>(wrappedSUL, timeLimit);
            LOGGER.info("Learning time limit set to {}", timeLimit);
        } else {
            LOGGER.info("Learning time limit already set to {}", timeLimit);
        }
        return this;
    }

    @Override
    public SULWrapper<I, O, E> setTestLimit(Long testLimit) {
        if (testLimit == null || testLimit <= 0L) {
            LOGGER.info("Learning test limit NOT set (provided value: {})", testLimit);
        } else if (this.testLimit == null) {
            this.testLimit = testLimit;
            wrappedSUL = new TestLimitWrapper<>(wrappedSUL, testLimit);
            LOGGER.info("Learning test limit set to {}", testLimit);
        } else {
            LOGGER.info("Learning test limit already set to {}", testLimit);
        }
        return this;
    }

    @Override
    public SULWrapper<I, O, E> setLoggingWrapper(String logPrefix) {
        wrappedSUL = new LoggingWrapper<>(wrappedSUL, logPrefix);
        return this;
    }


    @Override
    public SUL<I, O> getWrappedSUL() {
        return wrappedSUL;
    }

    @Override
    public Counter getInputCounter() {
        return inputCounter;
    }

    @Override
    public Counter getTestCounter() {
        return testCounter;
    }
}
