package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.sul.SUL;

import java.time.Duration;

/**
 * Interface for the wrapper of the {@link AbstractSul}.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public interface SulWrapper<S, I, O> {

    /**
     * Wrap the given abstract sul with a series of wrappers.
     *
     * @param abstractSul  the sul to be wrapped
     * @return             the updated SulWrapper instance
     */
    SulWrapper<S, I, O> wrap(AbstractSul<S, I, O> abstractSul);

    /**
     * Set the specified time limit using a designated wrapper.
     *
     * @param timeLimit  the time allowed for the underlying abstract sul to be active
     * @return           the updated SulWrapper instance
     */
    SulWrapper<S, I, O> setTimeLimit(Duration timeLimit);

    /**
     * Set the specified test (query) limit using a designated wrapper.
     *
     * @param testLimit  the number of tests that the underlying abstract sul is allowed to answer
     * @return           the updated SulWrapper instance
     */
    SulWrapper<S, I, O> setTestLimit(Long testLimit);

    /**
     * Adds a wrapper in order to log the inputs and outputs.
     * <p>
     * This method can be used multiple times to add many logging wrappers
     * after different set of inner wrappers or once as the outermost wrapper
     * before the {@link getWrappedSul}.
     *
     * @param logPrefix  a distinctive prefix before the actual logging, which
     *                   can be null or an empty string if not needed
     * @return  the updated SulWrapper instance
     */
    SulWrapper<S, I, O> setLoggingWrapper(String logPrefix);

    /**
     * Returns the final wrapped SUL Oracle instance.
     *
     * @return  the final wrapped SUL Oracle instance
     */
    SUL<I, O> getWrappedSul();

    /**
     * Returns the input counter used for counting all the inputs directed at
     * the underlying abstract sul.
     *
     * @return  the input counter of the underlying abstract sul
     */
    Counter getInputCounter();

    /**
     * Returns the test counter used for counting all the tests (queries)
     * directed at the underlying abstract sul.
     *
     * @return  the test counter of the underlying abstract sul
     */
    Counter getTestCounter();
}
