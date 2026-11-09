package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;

/**
 * The execution context of a test. It holds no protocol state, only the
 * receive timeout of the current step.
 */
public class MealyExecutionContext
    extends ExecutionContextStepped<MessageInput, MessageOutput, Void, StepContext<MessageInput, MessageOutput>> {

    private long receiveTimeout;

    public MealyExecutionContext() {
        super(null);
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    @Override
    protected StepContext<MessageInput, MessageOutput> buildStepContext() {
        return new StepContext<>(stepContexts.size());
    }
}
