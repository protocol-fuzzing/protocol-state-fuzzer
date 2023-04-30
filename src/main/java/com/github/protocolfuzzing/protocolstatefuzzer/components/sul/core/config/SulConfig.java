package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfigException;

import java.io.PrintWriter;

/**
 * The configuration regarding the sul.
 */
public abstract class SulConfig implements MapperConfigProvider, RunDescriptionPrinter {

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
     * Stores the JCommander Parameter -resetPort.
     * <p>
     * Port to which to send a reset command, in case a launch server is used to
     * execute the SUL.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-resetPort", description = "Port to which to send a reset command, "
            + "in case a launch server is used to execute the SUL")
    protected Integer resetPort = null;

    /**
     * Stores the JCommander Parameter -resetAddress.
     * <p>
     * Address to which to send a reset command, in case a launch server is
     * used to execute the SUL.
     * <p>
     * Default value: localhost.
     */
    @Parameter(names = "-resetAddress", description = "Address to which to send a reset command, "
            + "in case a launch server is used to execute the SUL")
    protected String resetAddress = "localhost";

    /**
     * Stores the JCommander Parameter -resetCommandWait.
     * <p>
     * Time (ms) waited after sending a reset command, in case a launch server
     * is used to execute the SUL.
     * <p>
     * Default value: 0L.
     */
    @Parameter(names = "-resetCommandWait", description = "Time (ms) waited after sending a reset command, "
            + "in case a launch server is used to execute the SUL")
    protected Long resetCommandWait = 0L;

    /**
     * Stores the JCommander Parameter -resetAck.
     * <p>
     * Wait for acknowledgement from the other side.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-resetAck", description = "Wait for acknowledgement from the other side")
    protected boolean resetAck = false;

    /**
     * Stores the configuration of the Mapper.
     */
    @ParametersDelegate
    protected MapperConfig mapperConfig;

    /**
     * Constructs a new instance by initializing {@link #mapperConfig} with the default
     * MapperConfig.
     */
    public SulConfig() {
        this.mapperConfig = new MapperConfig();
    }

    /**
     * Constructs a new instance from the given parameter.
     * <p>
     * If the given parameter is null then the default MapperConfig is used.
     *
     * @param mapperConfig  the configuration of the Mapper
     */
    public SulConfig(MapperConfig mapperConfig) {
        this.mapperConfig = mapperConfig == null ? new MapperConfig() : mapperConfig;
    }

    /**
     * Returns the role of the SUL under fuzzing that could be either "server" or "client".
     *
     * @return  the role of the SUL under fuzzing that could be either "server" or "client"
     */
    public abstract String getFuzzingRole();

    /**
     * Returns {@code true} if the SUL under fuzzing is a client implementation.
     *
     * @return  {@code true} if the SUL under fuzzing is a client implementation.
     */
    public abstract boolean isFuzzingClient();

    /**
     * Delegates if necessary the information provided in the parameter to other configurations.
     *
     * @param config  the configuration regarding the connection of the Mapper with the SUL process
     *
     * @throws MapperConnectionConfigException  in case an error occurs
     */
    public abstract void applyDelegate(MapperConnectionConfig config) throws MapperConnectionConfigException;

    @Override
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #responseWait}.
     *
     * @return  the stored value of {@link #responseWait}
     */
    public Long getResponseWait() {
        return responseWait;
    }

    /**
     * Sets the value of {@link #responseWait}.
     *
     * @param responseWait  the response wait value to be set
     */
    public void setResponseWait(Long responseWait) {
        this.responseWait = responseWait;
    }

    /**
     * Returns the stored value of {@link #inputResponseTimeout}.
     *
     * @return  the stored value of {@link #inputResponseTimeout}
     */
    public InputResponseTimeoutMap getInputResponseTimeout() {
        return inputResponseTimeout;
    }

    /**
     * Returns the stored value of {@link #command}.
     *
     * @return  the stored value of {@link #command}
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the stored value of {@link #terminateCommand}.
     *
     * @return  the stored value of {@link #terminateCommand}
     */
    public String getTerminateCommand() {
        return terminateCommand;
    }

    /**
     * Returns the stored value of {@link #processDir}.
     *
     * @return  the stored value of {@link #processDir}
     */
    public String getProcessDir() {
        return processDir;
    }

    /**
     * Returns the stored value of {@link #processTrigger}.
     *
     * @return  the stored value of {@link #processTrigger}
     */
    public ProcessLaunchTrigger getProcessTrigger() {
        return processTrigger;
    }

    /**
     * Returns the stored value of {@link #startWait}.
     *
     * @return  the stored value of {@link #startWait}
     */
    public Long getStartWait() {
        return startWait;
    }

    /**
     * Sets the value of {@link #startWait}.
     *
     * @param startWait  the start wait value to be set
     */
    public void setStartWait(Long startWait) {
        this.startWait = startWait;
    }

    /**
     * Returns the stored value of {@link #resetPort}.
     *
     * @return  the stored value of {@link #resetPort}
     */
    public Integer getResetPort() {
        return resetPort;
    }

    /**
     * Returns the stored value of {@link #resetAddress}.
     *
     * @return  the stored value of {@link #resetAddress}
     */
    public String getResetAddress() {
        return resetAddress;
    }

    /**
     * Returns the stored value of {@link #resetCommandWait}.
     *
     * @return  the stored value of {@link #resetCommandWait}
     */
    public Long getResetCommandWait() {
        return resetCommandWait;
    }

    /**
     * Returns the stored value of {@link #resetAck}.
     *
     * @return  the stored value of {@link #resetAck}
     */
    public boolean isResetAck() {
        return resetAck;
    }

    /**
     * Returns the stored value of {@link #redirectOutputStreams}.
     *
     * @return  the stored value of {@link #redirectOutputStreams}
     */
    public boolean isRedirectOutputStreams() {
        return redirectOutputStreams;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulConfig Parameters");
        printWriter.println("Response Wait: " + getResponseWait());
        printWriter.println("Input Response Timeout: " + getInputResponseTimeout());
        printWriter.println("Command: " + getCommand());
        printWriter.println("Terminate Command: " + getTerminateCommand());
        printWriter.println("Process Dir: " + getProcessDir());
        printWriter.println("Process Trigger: " + getProcessTrigger());
        printWriter.println("Start Wait: " + getStartWait());
        printWriter.println("Reset Port: " + getResetPort());
        printWriter.println("Reset Address: " + getResetAddress());
        printWriter.println("Reset Command Wait: " + getResetCommandWait());
        printWriter.println("Reset Ack: " + isResetAck());
        printWriter.println("Redirect Output Streams: " + isRedirectOutputStreams());
    }

    @Override
    public void printRunDescriptionRec(PrintWriter printWriter) {
        getMapperConfig().printRunDescription(printWriter);
    }
}
