package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutionContextStepped implements ExecutionContext {

	protected List<StepContext> stepContexts;
	protected boolean enabled = true;
	protected State state;

	public ExecutionContextStepped(State state) {
		stepContexts = new ArrayList<>();
		this.state = state;
	}

	@Override
	public State getState() {
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

	@Override
	public void setInput(AbstractInput input) {
		StepContext latestContext = getStepContext();
		if (latestContext != null) {
			latestContext.setInput(input);
		}
	}

	public void addStepContext() {
		stepContexts.add(new StepContext(stepContexts.size()));
	}

	public StepContext getStepContext() {
		if (!stepContexts.isEmpty())
			return stepContexts.get(stepContexts.size() - 1);
		return null;
	}

	public List<StepContext> getStepContexts() {
		return Collections.unmodifiableList(stepContexts);
	}

	public StepContext getStepContext(int ind) {
		return stepContexts.get(ind);
	}

	public int getStepCount() {
		return stepContexts.size();
	}
}
