package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.sul.SUL;

import java.time.Duration;

/**
 * Interface for the wrapper of the {@link AbstractSUL}.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <E>  the type of execution context
 */
public interface SULWrapper<I, O, E> {

    /**
     * Wrap the given abstract SUL with a series of wrappers.
     *
     * @param abstractSUL  the SUL to be wrapped
     * @return             the updated SULWrapper instance
     */
    SULWrapper<I, O, E> wrap(AbstractSUL<I, O, E> abstractSUL);

    /**
     * Set the specified time limit using a designated wrapper.
     *
     * @param timeLimit  the time allowed for the underlying abstract SUL to be active
     * @return           the updated SULWrapper instance
     */
    SULWrapper<I, O, E> setTimeLimit(Duration timeLimit);

    /**
     * Set the specified test (query) limit using a designated wrapper.
     *
     * @param testLimit  the number of tests that the underlying abstract SUL is allowed to answer
     * @return           the updated SULWrapper instance
     */
    SULWrapper<I, O, E> setTestLimit(Long testLimit);

    /**
     * Adds a wrapper in order to log the inputs and outputs.
     * <p>
     * This method can be used multiple times to add many logging wrappers
     * after different set of inner wrappers or once as the outermost wrapper
     * before the {@link getWrappedSUL}.
     *
     * @param logPrefix  a distinctive prefix before the actual logging, which
     *                   can be null or an empty string if not needed
     * @return  the updated SULWrapper instance
     */
    SULWrapper<I, O, E> setLoggingWrapper(String logPrefix);

    /**
     * Returns the final wrapped SUL Oracle instance.
     *
     * @return  the final wrapped SUL Oracle instance
     */
    SUL<I, O> getWrappedSUL();

    /**
     * Returns the input counter used for counting all the inputs directed at
     * the underlying abstract SUL.
     *
     * @return  the input counter of the underlying abstract SUL
     */
    Counter getInputCounter();

    /**
     * Returns the test counter used for counting all the tests (queries)
     * directed at the underlying abstract SUL.
     *
     * @return  the test counter of the underlying abstract SUL
     */
    Counter getTestCounter();
}
