package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;

import java.io.PrintWriter;

/**
 * The standard SUL configuration.
 */
public abstract class SulConfigStandard implements SulConfig {

    /**
     * Stores the JCommander Parameter -responseWait, -respWait.
     * <p>
     * Time (ms) the SUL spends waiting for a response.
     * <p>
     * Default value: 100L.
     */
    @Parameter(names = {"-responseWait", "-respWait"}, description = "Time (ms) the SUL spends waiting for a response")
    protected Long responseWait = 100L;

    /**
     * Stores the JCommander Parameter -inputResponseTimeout.
     * <p>
     * Time (ms) spent waiting for a response to a particular input.
     * Expected format is: "input1:value1,input2:value2...".
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-inputResponseTimeout", description = "Time (ms) spent waiting for a "
            + "response to a particular input. Expected format is: \"input1:value1,input2:value2...\" ",
            converter = InputResponseTimeoutConverter.class)
    protected InputResponseTimeoutMap inputResponseTimeout = null;

    /**
     * Stores the JCommander Parameter -command, -cmd.
     * <p>
     * Command for starting the client/server process.
     * <p>
     * Default value: null.
     */
    @Parameter(names = {"-command", "-cmd"}, description = "Command for starting the client/server process")
    protected String command = null;

    /**
     * Stores the JCommander Parameter -terminateCommand, -termCmd.
     * <p>
     * Command for terminating the client/server process.
     * If specified, it is used instead of {@link java.lang.Process#destroy()}.
     * <p>
     * Default value: null.
     */
    @Parameter(names = {"-terminateCommand", "-termCmd"}, description = "Command for terminating "
            + "the client/server process. If specified, it is used instead of java.lang.Process#destroy()")
    protected String terminateCommand = null;

    /**
     * Stores the JCommander Parameter -processDir.
     * <p>
     * The directory of the client/server process.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-processDir", description = "The directory of the client/server process")
    protected String processDir = null;

    /**
     * Stores the JCommander Parameter -redirectOutputStreams, -ros.
     * <p>
     * Redirects process output streams to STDOUT and STDERR.
     * <p>
     * Default value: false.
     */
    @Parameter(names = {"-redirectOutputStreams", "-ros"}, description = "Redirects process output "
             + "streams to STDOUT and STDERR")
    protected boolean redirectOutputStreams = false;

    /**
     * Stores the JCommander Parameter -processTrigger.
     * <p>
     * When is the process launched.
     * <p>
     * Default value: {@link ProcessLaunchTrigger#NEW_TEST}.
     */
    @Parameter(names = "-processTrigger", description = "When is the process launched")
    protected ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;

    /**
     * Stores the JCommander Parameter -startWait.
     * <p>
     * Time (ms) waited after executing the command to start the SUL process.
     * <p>
     * Default value: 0L.
     */
    @Parameter(names = "-startWait", description = "Time (ms) waited after executing the command to start the SUL process")
    protected Long startWait = 0L;

    /**
     * Stores the configuration of the Mapper.
     */
    @ParametersDelegate
    protected MapperConfig mapperConfig;

    /**
     * Stores the configuration of the SulAdapter.
     */
    @ParametersDelegate
    protected SulAdapterConfig sulAdapterConfig;

    /**
     * Constructs a new instance by initializing the {@link #mapperConfig} to the standard
     * MapperConfig and the {@link SulAdapterConfig} to the standard SulAdapterConfig.
     */
    public SulConfigStandard() {
        this.mapperConfig = new MapperConfigStandard();
        this.sulAdapterConfig = new SulAdapterConfigStandard();
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If any given parameter is null then the empty corresponding configuration is used.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulConfigStandard(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        this.mapperConfig = mapperConfig == null ? new MapperConfig(){} : mapperConfig;
        this.sulAdapterConfig = sulAdapterConfig == null ? new SulAdapterConfig(){} : sulAdapterConfig;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return  the stored value of {@link #mapperConfig}
     */
    @Override
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #sulAdapterConfig}.
     *
     * @return  the stored value of {@link #sulAdapterConfig}
     */
    @Override
    public SulAdapterConfig getSulAdapterConfig() {
        return sulAdapterConfig;
    }

    /**
     * Returns the stored value of {@link #responseWait}.
     *
     * @return  the stored value of {@link #responseWait}
     */
    @Override
    public Long getResponseWait() {
        return responseWait;
    }

    /**
     * Sets the value of {@link #responseWait}.
     *
     * @param responseWait  the response wait value to be set
     */
    @Override
    public void setResponseWait(Long responseWait) {
        this.responseWait = responseWait;
    }

    /**
     * Returns the stored value of {@link #inputResponseTimeout}.
     *
     * @return  the stored value of {@link #inputResponseTimeout}
     */
    @Override
    public InputResponseTimeoutMap getInputResponseTimeout() {
        return inputResponseTimeout;
    }

    /**
     * Returns the stored value of {@link #command}.
     *
     * @return  the stored value of {@link #command}
     */
    @Override
    public String getCommand() {
        return command;
    }

    /**
     * Returns the stored value of {@link #terminateCommand}.
     *
     * @return  the stored value of {@link #terminateCommand}
     */
    @Override
    public String getTerminateCommand() {
        return terminateCommand;
    }

    /**
     * Returns the stored value of {@link #processDir}.
     *
     * @return  the stored value of {@link #processDir}
     */
    @Override
    public String getProcessDir() {
        return processDir;
    }

    /**
     * Returns the stored value of {@link #redirectOutputStreams}.
     *
     * @return  the stored value of {@link #redirectOutputStreams}
     */
    @Override
    public boolean isRedirectOutputStreams() {
        return redirectOutputStreams;
    }

    /**
     * Returns the stored value of {@link #processTrigger}.
     *
     * @return  the stored value of {@link #processTrigger}
     */
    @Override
    public ProcessLaunchTrigger getProcessTrigger() {
        return processTrigger;
    }

    /**
     * Returns the stored value of {@link #startWait}.
     *
     * @return  the stored value of {@link #startWait}
     */
    @Override
    public Long getStartWait() {
        return startWait;
    }

    /**
     * Sets the value of {@link #startWait}.
     *
     * @param startWait  the start wait value to be set
     */
    @Override
    public void setStartWait(Long startWait) {
        this.startWait = startWait;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulConfigStandard Parameters");
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
    public void printRunDescriptionRec(PrintWriter printWriter) {
        getMapperConfig().printRunDescription(printWriter);
        getSulAdapterConfig().printRunDescription(printWriter);
    }
}
