package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

/**
 * Interface regarding the SUL configuration.
 */
public interface SulConfig extends RunDescriptionPrinter {

    /**
     * Returns the role of the SUL under fuzzing that could be either "server" or "client".
     *
     * @return  the role of the SUL under fuzzing that could be either "server" or "client"
     */
    String getFuzzingRole();

    /**
     * Returns {@code true} if the SUL under fuzzing is a client implementation.
     *
     * @return  {@code true} if the SUL under fuzzing is a client implementation.
     */
    boolean isFuzzingClient();

    /**
     * Delegates if necessary the information provided in the parameter to other configurations.
     *
     * @param <MCC>   the type of mapper connection configuration
     * @param config  the configuration regarding the connection of the Mapper with the SUL process
     */
    <MCC> void applyDelegate(MCC config);

    /**
     * Returns the associated MapperConfig.
     *
     * @return  the associated MapperConfig
     */
    MapperConfig getMapperConfig();

    /**
     * Returns the associated SulAdapterConfig.
     *
     * @return  the associated SulAdapterConfig
     */
    SulAdapterConfig getSulAdapterConfig();

    /**
     * Returns the time (ms) the SUL spends waiting for a response.
     *
     * @return  the time (ms) the SUL spends waiting for a response or null
     */
    Long getResponseWait();

    /**
     * Sets the time (ms) the SUL spends waiting for a response.
     *
     * @param responseWait  the response wait value to be set
     */
    void setResponseWait(Long responseWait);

    /**
     * Returns the time (ms) spent waiting for a response to a particular input.
     *
     * @return  the time (ms) spent waiting for a response to a particular input or null
     */
    InputResponseTimeoutMap getInputResponseTimeout();

    /**
     * Returns the command for starting the client/server process.
     *
     * @return  the command for starting the client/server process or null
     */
    String getCommand();

    /**
     * Returns the command for terminating the client/server process.
     *
     * @return  the command for terminating the client/server process or null
     */
    String getTerminateCommand();

    /**
     * Returns the directory of the client/server process.
     *
     * @return  the directory of the client/server process or null
     */
    String getProcessDir();

    /**
     * Indicates if the process output streams should be redirected to STDOUT and STDERR.
     *
     * @return  true if the process output streams should be redirected
     */
    boolean isRedirectOutputStreams();

    /**
     * Indicates when the process is launched.
     *
     * @return  a corresponding {@link ProcessLaunchTrigger}
     */
    ProcessLaunchTrigger getProcessTrigger();

    /**
     * Returns the time (ms) waited after executing the command to start the SUL process.
     *
     * @return  the time (ms) waited after executing the command to start the SUL process
     */
    Long getStartWait();

    /**
     * Sets the time (ms) waited after executing the command to start the SUL process.
     *
     * @param startWait  the start wait value to be set
     */
    void setStartWait(Long startWait);
}
