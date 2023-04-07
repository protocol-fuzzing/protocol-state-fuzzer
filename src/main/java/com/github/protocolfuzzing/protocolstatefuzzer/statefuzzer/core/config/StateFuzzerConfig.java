package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class StateFuzzerConfig implements StateFuzzerEnabler, TestRunnerEnabler, TimingProbeEnabler {

    @Parameter(names = { "-h", "-help" }, help = true, description = "Print usage for all existing commands")
    protected boolean help = false;

    @Parameter(names = "-debug", description = "Debug output shown")
    protected boolean debug = false;

    @Parameter(names = "-quiet", description = "No output shown")
    protected boolean quiet = false;

    @Parameter(names = "-output", description = "The directory in which results should be saved. The default is "
            + "output/o_<timestamp>")
    protected String outputDir = null;

    @ParametersDelegate
    protected PropertyResolver propertyResolver = PropertyResolver.getInstance();

    @ParametersDelegate
    protected LearnerConfig learnerConfig;

    @ParametersDelegate
    protected TestRunnerConfig testRunnerConfig;

    @ParametersDelegate
    protected TimingProbeConfig timingProbeConfig;

    public StateFuzzerConfig() {
        learnerConfig = new LearnerConfig();
        testRunnerConfig = new TestRunnerConfig();
        timingProbeConfig = new TimingProbeConfig();
    }

    public StateFuzzerConfig(LearnerConfig learnerConfig, TestRunnerConfig testRunnerConfig,
                             TimingProbeConfig timingProbeConfig) {
        this.learnerConfig = learnerConfig == null ? new LearnerConfig() : learnerConfig;
        this.testRunnerConfig = testRunnerConfig == null ? new TestRunnerConfig() : testRunnerConfig;
        this.timingProbeConfig = timingProbeConfig == null ? new TimingProbeConfig() : timingProbeConfig;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public String getOutputDir() {
        if (outputDir == null) {
            // initialize to default: output/o_<timestamp>
            String uniqueSubDir = "o_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            outputDir = Path.of("output", uniqueSubDir).toString();
        }
        return outputDir;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public LearnerConfig getLearnerConfig() {
        return learnerConfig;
    }

    public TestRunnerConfig getTestRunnerConfig() {
        return testRunnerConfig;
    }

    public TimingProbeConfig getTimingProbeConfig() {
        return timingProbeConfig;
    }

    public void printRunDescription(PrintWriter printWriter) {
        printRunDescriptionSelf(printWriter);
        printRunDescriptionRec(printWriter);
    }

    protected void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("StateFuzzerConfig Parameters");
        printWriter.println("Help: " + isHelp());
        printWriter.println("Debug: " + isDebug());
        printWriter.println("Quiet: " + isQuiet());
        printWriter.println("Output Directory: " + getOutputDir());
        printWriter.println("Fuzzing Client: " + isFuzzingClient());
    }

    protected void printRunDescriptionRec(PrintWriter printWriter) {
        getLearnerConfig().printRunDescription(printWriter);
        getSulConfig().printRunDescription(printWriter);
    }
}
