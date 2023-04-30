package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.api.SUL;

/**
 * Extension of the SulProcessWrapper for AbstractInput and AbstractOutput.
 */
public class AbstractProcessWrapper extends SulProcessWrapper<AbstractInput, AbstractOutput> {

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul        the inner sul to be wrapped
     * @param sulConfig  the configuration of the sul
     */
    public AbstractProcessWrapper(SUL<AbstractInput, AbstractOutput> sul, SulConfig sulConfig) {
        super(sul, sulConfig);
    }

    @Override
    public AbstractOutput step(AbstractInput in) {
        AbstractOutput output = super.step(in);
        output.setAlive(super.isAlive());
        return output;
    }
}
