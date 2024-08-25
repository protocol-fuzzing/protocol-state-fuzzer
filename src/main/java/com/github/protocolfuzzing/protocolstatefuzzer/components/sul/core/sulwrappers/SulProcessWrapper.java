package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;

import de.learnlib.sul.SUL;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SUL wrapper responsible for launching and terminating the SUL process.
 * Launches can be made at two distinct trigger points:
 * <ul>
 * <li> {@link ProcessLaunchTrigger#START} or
 * <li> {@link ProcessLaunchTrigger#NEW_TEST}
 * </ul>
 *
 *  @param <I>  the type of inputs
 *  @param <O>  the type of outputs
 */
public class SulProcessWrapper<I, O> implements SUL<I, O> {

    // TODO introduce ProcessConfig class and handlers should be a map from ProcessConfig to ProcessHandler

    /** Static map that stores process handlers associated with a {@link SulConfig#getCommand()}. */
    protected static final Map<String, ProcessHandler> handlers = new LinkedHashMap<>();

    /** Stores the handler of this instance. */
    protected ProcessHandler handler;

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    // TODO having the trigger here limits the trigger options; it should be outside

    /** Stores the trigger of this instance's handler specified in {@link SulConfig#getProcessTrigger()}. */
    protected ProcessLaunchTrigger trigger;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul        the inner sul to be wrapped
     * @param sulConfig  the configuration of the sul
     */
    public SulProcessWrapper(SUL<I, O> sul, SulConfig sulConfig) {
        this.sul = sul;

        if (!handlers.containsKey(sulConfig.getCommand())) {
            handlers.put(sulConfig.getCommand(), new ProcessHandler(sulConfig));
        }

        this.handler = handlers.get(sulConfig.getCommand());
        this.trigger = sulConfig.getProcessTrigger();

        if (trigger == ProcessLaunchTrigger.START && !handler.hasLaunched()) {
            handler.launchProcess();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> handler.terminateProcess()));
        }
    }

    /**
     * Runs before each test; used for setup.
     */
    @Override
    public void pre() {
        sul.pre();
        if (trigger == ProcessLaunchTrigger.NEW_TEST) {
            handler.launchProcess();
        }
    }

    /**
     * Runs after each test; used for shutdown.
     */
    @Override
    public void post() {
        sul.post();
        if (trigger == ProcessLaunchTrigger.NEW_TEST) {
            handler.terminateProcess();
        }
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul}.
     *
     * @param in  the input of the test
     * @return    the corresponding output
     *
     * @throws de.learnlib.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I in) {
        O output = sul.step(in);

        if (output instanceof AbstractOutput) {
            ((AbstractOutput) output).setAlive(isAlive());
        }

        return output;
    }

    /**
     * Returns {@code true} if {@link #handler} is alive.
     *
     * @return  {@code true} if {@link #handler} is alive
     */
    public boolean isAlive() {
        return handler.isAlive();
    }

}
