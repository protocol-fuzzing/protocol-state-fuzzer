package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;

public class AbstractProcessWrapper extends SulProcessWrapper<AbstractInput, AbstractOutput> {

    public AbstractProcessWrapper(SUL<AbstractInput, AbstractOutput> sul, SulConfig sulConfig) {
        super(sul, sulConfig);
    }

    @Override
    public AbstractOutput step(AbstractInput in) throws SULException {
        AbstractOutput output = super.step(in);
        output.setAlive(super.isAlive());
        return output;
    }
}
