package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

public class AbstractIsAliveWrapper implements SUL<AbstractInput, AbstractOutput> {

	protected SUL<AbstractInput, AbstractOutput> sul;
	protected boolean isAlive;
	protected AbstractOutput socketClosedOutput;

	public AbstractIsAliveWrapper(SUL<AbstractInput, AbstractOutput> sul, MapperConfig mapperConfig) {
		this.sul = sul;
		this.socketClosedOutput = mapperConfig.isSocketClosedAsTimeout() ?
				AbstractOutput.timeout() : AbstractOutput.socketClosed();
	}

	@Override
	public void pre() {
		sul.pre();
		isAlive = true;
	}

	@Override
	public void post() {
		sul.post();
	}

	@Override
	public AbstractOutput step(AbstractInput in) throws SULException {
		if (isAlive) {
			AbstractOutput out = sul.step(in);
			isAlive = out.isAlive();
			return out;
		} else {
			return socketClosedOutput;
		}
	}
}
