package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation of {@link ExecutionContext} comprising of many
 * step contexts that are added on each new input symbol as the execution proceeds.
 * <p>
 * Each time the last step context is the currently active one.
 *
 * @param <I>   the type of inputs
 * @param <O>   the type of outputs
 * @param <S>   the type of execution context's state
 * @param <SC>  the type of step context
 */
public abstract class ExecutionContextStepped<I, O, S, SC extends StepContext<I, O>> implements ExecutionContext<I, O, S> {

    /** The state of the outer execution context. */
    protected S state;

    /** Indicates if the context is enabled. */
    protected boolean enabled = true;

    /** The list of step contexts. */
    protected List<SC> stepContexts;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param state  the state of the context
     */
    public ExecutionContextStepped(S state) {
        stepContexts = new ArrayList<>();
        this.state = state;
    }

    @Override
    public S getState() {
        return this.state;
    }

    @Override
    public void disableExecution() {
        enabled = false;
    }

    @Override
    public void enableExecution() {
        enabled = true;
    }

    @Override
    public boolean isExecutionEnabled() {
        return enabled;
    }

    /**
     * Adds the given input to the last step context, which is currently active.
     *
     * @param input  the input to be added
     */
    @Override
    public void setInput(I input) {
        SC latestContext = getStepContext();
        if (latestContext != null) {
            latestContext.setInput(input);
        }
    }

    /**
     * Adds the given output to the last step context, which is currently active.
     *
     * @param output  the output to be added
     */
    @Override
    public void setOutput(O output) {
        SC latestContext = getStepContext();
        if (latestContext != null) {
            latestContext.setOutput(output);
        }
    }

    /**
     * Adds a new step context to {@link #stepContexts}.
     */
    public void addStepContext() {
        stepContexts.add(buildStepContext());
    }

    /**
     * Returns the last step context or null if there is not one.
     *
     * @return  the last step context or null if there is not one
     */
    public SC getStepContext() {
        if (stepContexts != null && !stepContexts.isEmpty()) {
            return stepContexts.get(stepContexts.size() - 1);
        }
        return null;
    }

    /**
     * Returns the list of {@link #stepContexts}.
     *
     * @return  the list of {@link #stepContexts}
     */
    public List<SC> getStepContexts() {
        return Collections.unmodifiableList(stepContexts);
    }

    /**
     * Returns the step context at the given index.
     *
     * @param index  the index of the context
     * @return       the step context at the given index
     *
     * @throws IndexOutOfBoundsException  if the specified index is out of bounds
     */
    public SC getStepContext(int index) {
        return stepContexts.get(index);
    }

    /**
     * Returns the size of {@link #stepContexts}.
     *
     * @return  the size of {@link #stepContexts}
     */
    public int getStepCount() {
        return stepContexts.size();
    }

    /**
     * Build a new step context from the current parameters.
     *
     * @return  a new step context from the current parameters
     */
    protected abstract SC buildStepContext();
}
