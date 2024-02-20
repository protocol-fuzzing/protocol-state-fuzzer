package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

import java.io.PrintWriter;

/**
 * The standard StateFuzzer configuration.
 */
public abstract class StateFuzzerConfigStandard implements StateFuzzerConfig {

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
     * Show output regarding only errors.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-quiet", description = "Show output regarding only errors")
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
     * Constructs a new instance, by creating a new empty {@link LearnerConfig},
     * a new empty {@link TestRunnerConfig} and a new empty {@link TimingProbeConfig}.
     */
    public StateFuzzerConfigStandard() {
        learnerConfig = new LearnerConfig(){};
        testRunnerConfig = new TestRunnerConfig(){};
        timingProbeConfig = new TimingProbeConfig(){};
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If a provided parameter is null, then the corresponding config is
     * initialized with a new empty corresponding configuration.
     * This means that a StateFuzzerConfig instance has always non-null inner configs.
     *
     * @param learnerConfig      the {@link LearnerConfig} implementing class
     * @param testRunnerConfig   the {@link TestRunnerConfig} implementing class
     * @param timingProbeConfig  the {@link TimingProbeConfig} implementing class
     */
    public StateFuzzerConfigStandard(LearnerConfig learnerConfig, TestRunnerConfig testRunnerConfig,
                             TimingProbeConfig timingProbeConfig) {
        this.learnerConfig = learnerConfig == null ? new LearnerConfig(){} : learnerConfig;
        this.testRunnerConfig = testRunnerConfig == null ? new TestRunnerConfig(){} : testRunnerConfig;
        this.timingProbeConfig = timingProbeConfig == null ? new TimingProbeConfig(){} : timingProbeConfig;
    }

    /**
     * Returns the value of {@link #help}.
     *
     * @return  the value of {@link #help}
     */
    @Override
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns the value of {@link #debug}.
     *
     * @return  the value of {@link #debug}
     */
    @Override
    public boolean isDebug() {
        return debug;
    }

    /**
     * Returns the value of {@link #quiet}.
     *
     * @return  the value of {@link #quiet}
     */
    @Override
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns the singleton PropertyResolver instance.
     *
     * @return  the singleton PropertyResolver instance
     */
    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public String getOutputDir() {
        if (outputDir == null) {
            outputDir = StateFuzzerConfig.super.createUniqueOutputDir();
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
        printWriter.println("StateFuzzerConfigStandard Parameters");
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
