package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.LoggingWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SulAdapterWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SulLivenessTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SulLivenessWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.SulProcessWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.TestLimitWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.TimeoutWrapper;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.sul.CounterSUL;
import de.learnlib.sul.SUL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * The standard implementation of {@link SulWrapper} using wrappers from the package
 * {@link com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers  sulwrappers}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public class SulWrapperStandard<I, O, E> implements SulWrapper<I, O, E> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The updated wrapped sul to be finally returned using {@link #getWrappedSul()}. */
    protected SUL<I, O> wrappedSul;

    /** The input counter used for the underlying abstract sul. */
    protected Counter inputCounter;

    /** The test counter used for the underlying abstract sul. */
    protected Counter testCounter;

    /** The time limit to be set only once using {@link #setTimeLimit(Duration)}. */
    protected Duration timeLimit;

    /** The test limit to be set only once using {@link #setTestLimit(Long)}. */
    protected Long testLimit;

    @Override
    public SulWrapper<I, O, E> wrap(AbstractSul<I, O, E> abstractSul) {
        wrappedSul = abstractSul;
        SulConfig sulConfig = abstractSul.getSulConfig();
        SulLivenessTracker sulLivenessTracker = new SulLivenessTracker(true);

        if (sulConfig.getCommand() != null) {
            wrappedSul = new SulProcessWrapper<>(wrappedSul, sulConfig, sulLivenessTracker);
        }

        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (abstractSul.getSulAdapter() == null) {
                throw new RuntimeException("Provided adapter port with a null SulAdapter in AbstractSul.");
            }

            wrappedSul = new SulAdapterWrapper<>(wrappedSul, abstractSul.getSulAdapter(), sulLivenessTracker);
            abstractSul.setDynamicPortProvider((DynamicPortProvider) wrappedSul);
        }

        O socketClosed;
        if (abstractSul.getMapper().getMapperConfig().isSocketClosedAsTimeout()) {
            socketClosed = abstractSul.getMapper().getOutputBuilder().buildTimeout();
        } else {
            socketClosed = abstractSul.getMapper().getOutputBuilder().buildSocketClosed();
        }

        wrappedSul = new SulLivenessWrapper<>(wrappedSul, sulLivenessTracker, socketClosed);

        wrappedSul = new CounterSUL<>(wrappedSul);

        inputCounter = CounterSUL.class.cast(wrappedSul).getSymbolCounter();
        testCounter = CounterSUL.class.cast(wrappedSul).getResetCounter();
        return this;
    }

    @Override
    public SulWrapper<I, O, E> setTimeLimit(Duration timeLimit) {
        if (timeLimit == null || timeLimit.isNegative() || timeLimit.isZero()) {
            LOGGER.info("Learning time limit NOT set (provided value: {})", timeLimit);
        } else if (this.timeLimit == null) {
            this.timeLimit = timeLimit;
            wrappedSul = new TimeoutWrapper<>(wrappedSul, timeLimit);
            LOGGER.info("Learning time limit set to {}", timeLimit);
        } else {
            LOGGER.info("Learning time limit already set to {}", timeLimit);
        }
        return this;
    }

    @Override
    public SulWrapper<I, O, E> setTestLimit(Long testLimit) {
        if (testLimit == null || testLimit <= 0L) {
            LOGGER.info("Learning test limit NOT set (provided value: {})", testLimit);
        } else if (this.testLimit == null) {
            this.testLimit = testLimit;
            wrappedSul = new TestLimitWrapper<>(wrappedSul, testLimit);
            LOGGER.info("Learning test limit set to {}", testLimit);
        } else {
            LOGGER.info("Learning test limit already set to {}", testLimit);
        }
        return this;
    }

    @Override
    public SulWrapper<I, O, E> setLoggingWrapper(String logPrefix) {
        wrappedSul = new LoggingWrapper<>(wrappedSul, logPrefix);
        return this;
    }


    @Override
    public SUL<I, O> getWrappedSul() {
        return wrappedSul;
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
