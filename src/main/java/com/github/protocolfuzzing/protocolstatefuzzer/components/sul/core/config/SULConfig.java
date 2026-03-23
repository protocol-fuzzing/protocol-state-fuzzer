package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Interface regarding the SUL configuration.
 */
public interface SULConfig extends RunDescriptionPrinter {

    /**
     * Return a new instance of SUlConfig with the given threadId.
     * @param threadId  used to change the port number
     * @return          a new instance of SUlConfig
     */
    default SULConfig cloneWithThreadId(int threadId){
        throw new UnsupportedOperationException("Not implemented yet! Your need to override this method.");
    }

    /**
     * Returns the role of the SUL under fuzzing that could be either "client" or "server".
     * <p>
     * Default value: {@code "client"}.
     *
     * @return  the role of the SUL under fuzzing that could be either "client" or "server"
     */
    default String getFuzzingRole() {
        return "client";
    }

    /**
     * Returns {@code true} if the SUL under fuzzing is a client implementation.
     * <p>
     * Default value: {@code true}.
     *
     * @return  {@code true} if the SUL under fuzzing is a client implementation.
     */
    default boolean isFuzzingClient() {
        return true;
    }

    /**
     * Delegates if necessary the information provided in the parameter to other configurations.
     * <p>
     * Default value: does nothing.
     *
     * @param <MCC>   the type of mapper connection configuration
     * @param config  the configuration regarding the connection of the Mapper with the SUL process
     */
    default <MCC> void applyDelegate(MCC config) {
    }

    /**
     * Returns the associated MapperConfig.
     * <p>
     * Default value: new empty MapperConfig.
     *
     * @return  the associated MapperConfig
     */
    default MapperConfig getMapperConfig() {
        return new MapperConfig(){};
    }

    /**
     * Returns the associated SULAdapterConfig.
     * <p>
     * Default value: new empty SULAdapterConfig.
     *
     * @return  the associated SULAdapterConfig
     */
    default SULAdapterConfig getSULAdapterConfig() {
        return new SULAdapterConfig(){};
    }

    /**
     * Returns the time (ms) the SUL spends waiting for a response.
     * <p>
     * Default value: 100L.
     *
     * @return  the time (ms) the SUL spends waiting for a response or null
     */
    default Long getResponseWait() {
        return 100L;
    }

    /**
     * Sets the time (ms) the SUL spends waiting for a response.
     * <p>
     * Default: does nothing.
     *
     * @param responseWait  the response wait value to be set
     */
    default void setResponseWait(Long responseWait) {
    }

    /**
     * Returns the map that indicates for each input the given time (ms) that
     * must be spent waiting for a response (when this input is sent).
     * <p>
     * It is to be used for a per-input timeout specification.
     * <p>
     * Default value: null.
     *
     * @return  the map from input names to response times (ms)
     */
    default Map<String, Long> getInputResponseTimeout() {
        return null;
    }

    /**
     * Returns the command for starting the client/server process.
     * <p>
     * Default value: null.
     *
     * @return  the command for starting the client/server process or null
     */
    default String getCommand() {
        return null;
    }

    /**
     * Returns the command for terminating the client/server process.
     * <p>
     * Default value: null.
     *
     * @return  the command for terminating the client/server process or null
     */
    default String getTerminateCommand() {
        return null;
    }

    /**
     * Returns the directory of the client/server process.
     * <p>
     * Default value: null.
     *
     * @return  the directory of the client/server process or null
     */
    default String getProcessDir() {
        return null;
    }

    /**
     * Indicates if the process output streams should be redirected to STDOUT and STDERR.
     * <p>
     * Default value: false.
     *
     * @return  true if the process output streams should be redirected
     */
    default boolean isRedirectOutputStreams() {
        return false;
    }

    /**
     * Indicates when the process is launched.
     * <p>
     * Default value: {@link ProcessLaunchTrigger#NEW_TEST}.
     *
     * @return  a corresponding {@link ProcessLaunchTrigger}
     */
    default ProcessLaunchTrigger getProcessTrigger() {
        return ProcessLaunchTrigger.NEW_TEST;
    }

    /**
     * Returns the time (ms) waited after executing the command to start the SUL process.
     * <p>
     * Default value: 0L.
     *
     * @return  the time (ms) waited after executing the command to start the SUL process
     */
    default Long getStartWait() {
        return 0L;
    }

    /**
     * Sets the time (ms) waited after executing the command to start the SUL process.
     * <p>
     * Default: does nothing.
     *
     * @param startWait  the start wait value to be set
     */
    default void setStartWait(Long startWait) {
    }

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SULConfig Parameters");
        printWriter.println("Fuzzing Role: " + getFuzzingRole());
        printWriter.println("Fuzzing Client: " + isFuzzingClient());
        printWriter.println("Response Wait: " + getResponseWait());
        printWriter.println("Input Response Timeout: " + getInputResponseTimeout());
        printWriter.println("Command: " + getCommand());
        printWriter.println("Terminate Command: " + getTerminateCommand());
        printWriter.println("Process Dir: " + getProcessDir());
        printWriter.println("Redirect Output Streams: " + isRedirectOutputStreams());
        printWriter.println("Process Trigger: " + getProcessTrigger());
        printWriter.println("Start Wait: " + getStartWait());
    }

    @Override
    default void printRunDescriptionRec(PrintWriter printWriter) {
        if (getMapperConfig() != null) {
            getMapperConfig().printRunDescription(printWriter);
        }

        if (getSULAdapterConfig() != null) {
            getSULAdapterConfig().printRunDescription(printWriter);
        }
    }
}
