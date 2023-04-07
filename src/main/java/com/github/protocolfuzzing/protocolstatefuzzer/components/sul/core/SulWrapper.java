package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.api.SUL;
import de.learnlib.filter.statistic.Counter;

import java.time.Duration;

public interface SulWrapper {
    SulWrapper wrap(AbstractSul abstractSul);

    SulWrapper setTimeLimit(Duration timeLimit);

    SulWrapper setTestLimit(Long testLimit);

    SUL<AbstractInput, AbstractOutput> getWrappedSul();

    Counter getInputCounter();

    Counter getTestCounter();
}
