package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.*;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.api.SUL;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * The standard implementation of {@link SulWrapper} using wrappers from the package
 * {@link com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers  sulwrappers}.
 */
public class SulWrapperStandard implements SulWrapper {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The updated wrapped sul to be finally returned using {@link #getWrappedSul()}. */
    protected SUL<AbstractInput, AbstractOutput> wrappedSul;

    /** The input counter used for the underlying abstract sul. */
    protected Counter inputCounter;

    /** The test counter used for the underlying abstract sul. */
    protected Counter testCounter;

    /** The time limit to be set only once using {@link #setTimeLimit(Duration)}. */
    protected Duration timeLimit;

    /** The test limit to be set only once using {@link #setTestLimit(Long)}. */
    protected Long testLimit;

    @Override
    public SulWrapper wrap(AbstractSul abstractSul) {
        wrappedSul = abstractSul;
        SulConfig sulConfig = abstractSul.getSulConfig();

        if (sulConfig.getCommand() != null) {
            wrappedSul = new AbstractProcessWrapper(wrappedSul, sulConfig);
        }

        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (abstractSul.getSulAdapter() == null) {
                throw new RuntimeException("Provided adapter port with a null SulAdapter in AbstractSul.");
            }

            wrappedSul = new SulAdapterWrapper(wrappedSul, abstractSul.getSulAdapter());
            abstractSul.setDynamicPortProvider((DynamicPortProvider) wrappedSul);
        }

        wrappedSul = new AbstractIsAliveWrapper(wrappedSul, sulConfig.getMapperConfig());

        wrappedSul = new SymbolCounterSUL<>("input counter", wrappedSul);
        inputCounter = ((SymbolCounterSUL<AbstractInput, AbstractOutput>) wrappedSul).getStatisticalData();

        wrappedSul = new ResetCounterSUL<>("test counter", wrappedSul);
        testCounter = ((ResetCounterSUL<AbstractInput, AbstractOutput>) wrappedSul).getStatisticalData();
        return this;
    }

    @Override
    public SulWrapper setTimeLimit(Duration timeLimit) {
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
    public SulWrapper setTestLimit(Long testLimit) {
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
    public SulWrapper setLoggingWrapper(String logPrefix) {
        wrappedSul = new LoggingWrapper<>(wrappedSul, logPrefix);
        return this;
    }


    @Override
    public SUL<AbstractInput, AbstractOutput> getWrappedSul() {
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
