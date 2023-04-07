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

public abstract class SulConfig implements MapperConfigProvider, RunDescriptionPrinter {

    @Parameter(names = {"-responseWait", "-respWait"}, description = "Time (ms) the SUL spends waiting for a response")
    protected Long responseWait = 100L;

    @Parameter(names = "-inputResponseTimeout", description = "Time (ms) the SUL spends waiting for a "
            + "response to a particular input. Expected format is: \"input1:value1,input2:value2...\" ",
            converter = InputResponseTimeoutConverter.class)
    protected InputResponseTimeoutMap inputResponseTimeout;

    @Parameter(names = {"-command", "-cmd"}, description = "Command for starting the client/server process")
    protected String command = null;

    @Parameter(names = {"-terminateCommand", "-termCmd"}, description = "Command for terminating "
            + "the client/server process. If specified, it is used instead of java.lang.Process#destroy()")
    protected String terminateCommand = null;

    @Parameter(names = "-processDir", description = "The directory of the client/server process")
    protected String processDir = null;

    @Parameter(names = {"-redirectOutputStreams", "-ros"}, description = "Redirects process output "
             + "streams to STDOUT and STDERR.")
    protected boolean redirectOutputStreams = false;

    @Parameter(names = "-processTrigger", description = "When is the process launched")
    protected ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;

    @Parameter(names = "-startWait", description = "Time (ms) waited after executing the command to start the SUL process.")
    protected Long startWait = 0L;

    // In case a launch server is used to execute the SUL
    @Parameter(names = "-resetPort", description = "Port to which to send a reset command")
    protected Integer resetPort = null;

    @Parameter(names = "-resetAddress", description = "Address to which to send a reset command")
    protected String resetAddress = "localhost";

    @Parameter(names = "-resetCommandWait", description = "Time (ms) waited after sending a reset command")
    protected Long resetCommandWait = 0L;

    @Parameter(names = "-resetAck", description = "Wait for acknowledgement from the other side")
    protected boolean resetAck = false;

    @ParametersDelegate
    protected MapperConfig mapperConfig;

    public SulConfig() {
        this.mapperConfig = new MapperConfig();
    }

    public SulConfig(MapperConfig mapperConfig) {
        this.mapperConfig = mapperConfig == null ? new MapperConfig() : mapperConfig;
    }

    public abstract String getFuzzingRole();

    public abstract boolean isFuzzingClient();

    public abstract void applyDelegate(MapperConnectionConfig config) throws MapperConnectionConfigException;

    @Override
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    public Long getResponseWait() {
        return responseWait;
    }

    public void setResponseWait(Long responseWait) {
        this.responseWait = responseWait;
    }

    public InputResponseTimeoutMap getInputResponseTimeout() {
        return inputResponseTimeout;
    }

    public String getCommand() {
        return command;
    }

    public String getTerminateCommand() {
        return terminateCommand;
    }

    public String getProcessDir() {
        return processDir;
    }

    public ProcessLaunchTrigger getProcessTrigger() {
        return processTrigger;
    }

    public Long getStartWait() {
        return startWait;
    }

    public void setStartWait(Long startWait) {
        this.startWait = startWait;
    }

    public Integer getResetPort() {
        return resetPort;
    }

    public String getResetAddress() {
        return resetAddress;
    }

    public Long getResetCommandWait() {
        return resetCommandWait;
    }

    public boolean isResetAck() {
        return resetAck;
    }

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
