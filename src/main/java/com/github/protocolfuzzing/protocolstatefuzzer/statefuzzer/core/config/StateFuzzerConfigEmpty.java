package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;

import java.io.PrintWriter;

/**
 * The empty StateFuzzer configuration without any JCommander Parameters.
 */
public abstract class StateFuzzerConfigEmpty implements StateFuzzerConfig {

    /**
     * The directory in which results should be saved with the default format
     * of {@link StateFuzzerConfig#createUniqueOutputDir()}.
     */
    protected String outputDir = StateFuzzerConfig.super.createUniqueOutputDir();

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
    public StateFuzzerConfigEmpty() {
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
    public StateFuzzerConfigEmpty(LearnerConfig learnerConfig, TestRunnerConfig testRunnerConfig,
                                    TimingProbeConfig timingProbeConfig) {
        this.learnerConfig = learnerConfig == null ? new LearnerConfig(){} : learnerConfig;
        this.testRunnerConfig = testRunnerConfig == null ? new TestRunnerConfig(){} : testRunnerConfig;
        this.timingProbeConfig = timingProbeConfig == null ? new TimingProbeConfig(){} : timingProbeConfig;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isHelp() {
        return false;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isDebug() {
        return false;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isQuiet() {
        return false;
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public String getOutputDir() {
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
        printWriter.println("StateFuzzerConfigEmpty Non-Explicit Parameters");
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
