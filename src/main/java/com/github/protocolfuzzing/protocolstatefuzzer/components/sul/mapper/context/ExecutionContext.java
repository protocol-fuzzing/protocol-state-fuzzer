package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;

public interface ExecutionContext {
    State getState();

    void disableExecution();

    void enableExecution();

    boolean isExecutionEnabled();

    void setInput(AbstractInput input);
}
