package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import de.learnlib.api.SUL;

/**
 * SUL Wrapper that checks for the liveness of the wrapped sul.
 */
public class AbstractIsAliveWrapper implements SUL<AbstractInput, AbstractOutput> {

    /** Stores the constructor parameter. */
    protected SUL<AbstractInput, AbstractOutput> sul;

    /** Indicates if the {@link #sul} is found to be alive or not. */
    protected boolean isAlive;

    /** Stores the output to be returned when the {@link #sul} is found to have terminated. */
    protected AbstractOutput socketClosedOutput;

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * The method {@link MapperConfig#isSocketClosedAsTimeout()} is used to
     * determine which output to return.
     *
     * @param sul           the sul to be wrapped
     * @param mapperConfig  the configuration of the Mapper
     */
    public AbstractIsAliveWrapper(SUL<AbstractInput, AbstractOutput> sul, MapperConfig mapperConfig) {
        this.sul = sul;
        this.socketClosedOutput = mapperConfig.isSocketClosedAsTimeout() ?
                AbstractOutput.timeout() : AbstractOutput.socketClosed();
    }

    /**
     * Runs before each test; used for setup.
     */
    @Override
    public void pre() {
        sul.pre();
        isAlive = true;
    }

    /**
     * Runs after each test; used for shutdown.
     */
    @Override
    public void post() {
        sul.post();
    }

    /**
     * Propagates the inputs of a test to the inner sul and checks its
     * aliveness via the {@link AbstractOutput#isAlive()} of its output.
     *
     * @param in  the input of the test
     * @return    the corresponding output or {@link #socketClosedOutput} in case
     *            the {@link #sul} is observed to have terminated
     *
     * @throws de.learnlib.api.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public AbstractOutput step(AbstractInput in) {
        if (isAlive) {
            AbstractOutput out = sul.step(in);
            isAlive = out.isAlive();
            return out;
        } else {
            return socketClosedOutput;
        }
    }
}
