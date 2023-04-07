package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A SUL wrapper responsible for launching/terminating the process starting up
 * the SUL. Launches can be made at two distinct trigger points:
 * (1) once at the start, with termination taking place at the end of learning/testing
 * (2) before executing each test, with termination done after the test has been executed
 */
public class SulProcessWrapper<I, O> implements SUL<I, O> {

    protected static Map<String, ProcessHandler> handlers = new LinkedHashMap<>();

    protected ProcessHandler handler;

    protected SUL<I, O> sul;
    // TODO having the trigger here is not nice since it limits the trigger
    // options. Ideally we would have it outside.
    protected ProcessLaunchTrigger trigger;

    // TODO We should pass here ProcessConfig class, handlers becoming a map
    // from ProcessConfig to ProcessHandler.
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

    @Override
    public void pre() {
        sul.pre();
        if (trigger == ProcessLaunchTrigger.NEW_TEST) {
            handler.launchProcess();
        }
    }

    @Override
    public void post() {
        sul.post();
        if (trigger == ProcessLaunchTrigger.NEW_TEST) {
            handler.terminateProcess();
        }
    }

    @Override
    public O step(I in) throws SULException {
        return sul.step(in);
    }

    public boolean isAlive() {
        return handler.isAlive();
    }

}
