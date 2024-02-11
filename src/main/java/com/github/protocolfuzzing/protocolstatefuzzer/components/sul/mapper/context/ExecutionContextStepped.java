package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ExecutionContext} comprising of many step contexts
 * that are added on each new input symbol as the execution proceeds.
 * <p>
 * Each time the last step context is the currently active one.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 */
public class ExecutionContextStepped<S, I> implements ExecutionContext<S, I> {

    /** The state of the outer execution context. */
    protected S state;

    /** Indicates if the context is enabled. */
    protected boolean enabled = true;

    /** The list of step contexts. */
    protected List<StepContext<I>> stepContexts;

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
     * @param input  the input symbol to be added
     */
    @Override
    public void setInput(I input) {
        StepContext<I> latestContext = getStepContext();
        if (latestContext != null) {
            latestContext.setInput(input);
        }
    }

    /**
     * Adds a new step context to {@link #stepContexts}.
     */
    public void addStepContext() {
        stepContexts.add(new StepContext<I>(stepContexts.size()));
    }

    /**
     * Returns the last step context or null if there is not one.
     *
     * @return  the last step context or null if there is not one
     */
    public StepContext<I> getStepContext() {
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
    public List<StepContext<I>> getStepContexts() {
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
    public StepContext<I> getStepContext(int index) {
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
}
