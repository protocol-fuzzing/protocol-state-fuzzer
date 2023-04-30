package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.api.SUL;
import de.learnlib.filter.statistic.Counter;

import java.time.Duration;

/**
 * Interface for the wrapper of the {@link AbstractSul}.
 */
public interface SulWrapper {

    /**
     * Wrap the given abstract sul with a series of wrappers.
     *
     * @param abstractSul  the sul to be wrapped
     * @return             the updated SulWrapper instance
     */
    SulWrapper wrap(AbstractSul abstractSul);

    /**
     * Set the specified time limit using a designated wrapper.
     *
     * @param timeLimit  the time allowed for the underlying abstract sul to be active
     * @return           the updated SulWrapper instance
     */
    SulWrapper setTimeLimit(Duration timeLimit);

    /**
     * Set the specified test (query) limit using a designated wrapper.
     *
     * @param testLimit  the number of tests that the underlying abstract sul is allowed to answer
     * @return           the updated SulWrapper instance
     */
    SulWrapper setTestLimit(Long testLimit);

    /**
     * Returns the final wrapped SUL Oracle instance.
     *
     * @return  the final wrapped SUL Oracle instance
     */
    SUL<AbstractInput, AbstractOutput> getWrappedSul();

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
