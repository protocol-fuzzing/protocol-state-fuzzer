package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract class of the main configuration that implements all Enablers
 * and allows state fuzzing and testing.
 */
public abstract class StateFuzzerConfig implements StateFuzzerEnabler, TestRunnerEnabler, TimingProbeEnabler {

    /**
     * Stores the JCommander Parameter -h, -help.
     * <p>
     * Print usage for all existing commands.
     * <p>
     * Default value: false.
     */
    @Parameter(names = { "-h", "-help" }, help = true, description = "Print usage for all existing commands")
    protected boolean help = false;

    /**
     * Stores the JCommander Parameter -debug.
     * <p>
     * Show debug output.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-debug", description = "Show debug output")
    protected boolean debug = false;

    /**
     * Stores the JCommander Parameter -quiet.
     * <p>
     * Show no output.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-quiet", description = "Show no output")
    protected boolean quiet = false;

    /**
     * Stores the JCommander Parameter -output.
     * <p>
     * The directory in which results should be saved.
     * <p>
     * Default value: {@code output/o_<timestamp>}.
     */
    @Parameter(names = "-output", description = "The directory in which results should be saved. The default is output/o_<timestamp>")
    protected String outputDir = null;

    /**
     * Stores the singleton instance of the {@link PropertyResolver}.
     */
    @ParametersDelegate
    protected PropertyResolver propertyResolver = PropertyResolver.getInstance();

    /**
     * Stores the specified LearnerConfig.
     */
    @ParametersDelegate
    protected LearnerConfig learnerConfig;

    /**
     * Stores the specified TestRunnerConfig.
     */
    @ParametersDelegate
    protected TestRunnerConfig testRunnerConfig;

    /**
     * Stores the specified TimingProbeConfig.
     */
    @ParametersDelegate
    protected TimingProbeConfig timingProbeConfig;

    /**
     * Constructs a new instance, by creating a new {@link LearnerConfig},
     * a new {@link TestRunnerConfig} and a new {@link TimingProbeConfig}.
     */
    public StateFuzzerConfig() {
        learnerConfig = new LearnerConfig();
        testRunnerConfig = new TestRunnerConfig();
        timingProbeConfig = new TimingProbeConfig();
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If a provided parameter is null, then the corresponding config is
     * initialized with a new instance of the corresponding base class.
     * This means that a StateFuzzerConfig instance has always non-null inner configs.
     *
     * @param learnerConfig      the {@link LearnerConfig} (sub)class
     * @param testRunnerConfig   the {@link TestRunnerConfig} (sub)class
     * @param timingProbeConfig  the {@link TimingProbeConfig} (sub)class
     */
    public StateFuzzerConfig(LearnerConfig learnerConfig, TestRunnerConfig testRunnerConfig,
                             TimingProbeConfig timingProbeConfig) {
        this.learnerConfig = learnerConfig == null ? new LearnerConfig() : learnerConfig;
        this.testRunnerConfig = testRunnerConfig == null ? new TestRunnerConfig() : testRunnerConfig;
        this.timingProbeConfig = timingProbeConfig == null ? new TimingProbeConfig() : timingProbeConfig;
    }

    /**
     * Returns the value of {@link #help}.
     *
     * @return  the value of {@link #help}
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns the value of {@link #debug}.
     *
     * @return  the value of {@link #debug}
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Returns the value of {@link #quiet}.
     *
     * @return  the value of {@link #quiet}
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns the singleton PropertyResolver instance.
     *
     * @return  the singleton PropertyResolver instance
     */
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public String getOutputDir() {
        if (outputDir == null) {
            // initialize to default: output/o_<timestamp>
            String uniqueSubDir = "o_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            outputDir = "output" + File.separator + uniqueSubDir;
        }
        return outputDir;
    }

    @Override
    public LearnerConfig getLearnerConfig() {
        return learnerConfig;
    }

    @Override
    public TestRunnerConfig getTestRunnerConfig() {
        return testRunnerConfig;
    }

    @Override
    public TimingProbeConfig getTimingProbeConfig() {
        return timingProbeConfig;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("StateFuzzerConfig Parameters");
        printWriter.println("Help: " + isHelp());
        printWriter.println("Debug: " + isDebug());
        printWriter.println("Quiet: " + isQuiet());
        printWriter.println("Output Directory: " + getOutputDir());
        printWriter.println("Fuzzing Client: " + isFuzzingClient());
    }

    @Override
    public void printRunDescriptionRec(PrintWriter printWriter) {
        getLearnerConfig().printRunDescription(printWriter);
        getSulConfig().printRunDescription(printWriter);
    }
}
