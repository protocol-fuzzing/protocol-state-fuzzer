package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Interface of the main configuration that extends all Enablers
 * and allows state fuzzing and testing.
 */
public interface StateFuzzerConfig extends StateFuzzerEnabler, TimingProbeEnabler {

    /**
     * Indicates if the help usage should be printed.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the help usage should be printed
     */
    default boolean isHelp() {
        return false;
    }

    /**
     * Indicates if the logging level should be set to DEBUG.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the logging level should be set to DEBUG
     */
    default boolean isDebug() {
        return false;
    }

    /**
     * Indicates if the logging level should be set high enough,
     * in order to let the output be quiet.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the logging level should be set high enough,
     *          in order to let the output be quiet
     */
    default boolean isQuiet() {
        return false;
    }

    /**
     * Returns the singleton PropertyResolver instance.
     * <p>
     * Default: the singleton instance.
     *
     * @return  the singleton PropertyResolver instance
     */
    default PropertyResolver getPropertyResolver() {
        return PropertyResolver.getInstance();
    }

    /**
     * Returns a unique path of a directory of the format {@code output/o_<timestamp>},
     * in which results can be saved.
     *
     * @return  a unique directory path, in which results can be saved
     */
    default String createUniqueOutputDir() {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now(ZoneId.systemDefault()));
        String outputDir = "output" + File.separator + "o_" + timestamp;
        return outputDir;
    }

    /**
     * Returns the directory, in which results should be saved.
     * <p>
     * Default value: the result of {@link #createUniqueOutputDir()}.
     *
     * @return  the output directory, in which results should be saved
     */
    @Override
    default String getOutputDir() {
        return createUniqueOutputDir();
    }

    /**
     * Returns {@code true} if analysis concerns a client implementation.
     * <p>
     * Default value: true.
     *
     * @return  {@code true} if analysis concerns a client implementation
     */
    @Override
    default boolean isFuzzingClient() {
        return true;
    }

    /**
     * Returns the LearnerConfig.
     * <p>
     * Default value: a new empty LearnerConfig.
     *
     * @return  the LearnerConfig
     */
    @Override
    default LearnerConfig getLearnerConfig() {
        return new LearnerConfig(){};
    }

    /**
     * Returns the SulConfig.
     * <p>
     * Default value: a new empty SulConfig.
     *
     * @return  the SulConfig
     */
    @Override
    default SulConfig getSulConfig() {
        return new SulConfig(){};
    }

    /**
     * Returns the TestRunnerConfig.
     * <p>
     * Default value: a new empty TestRunnerConfig.
     *
     * @return  the TestRunnerConfig
     */
    @Override
    default TestRunnerConfig getTestRunnerConfig() {
        return new TestRunnerConfig(){};
    }

    /**
     * Returns the TimingProbeConfig.
     * <p>
     * Default value: a new empty TimingProbeConfig.
     *
     * @return  the TimingProbeConfig
     */
    @Override
    default TimingProbeConfig getTimingProbeConfig() {
        return new TimingProbeConfig(){};
    }

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("StateFuzzerConfig Parameters");
        printWriter.println("Help: " + isHelp());
        printWriter.println("Debug: " + isDebug());
        printWriter.println("Quiet: " + isQuiet());
        printWriter.println("Output Directory: " + getOutputDir());
        printWriter.println("Fuzzing Client: " + isFuzzingClient());
    }

    @Override
    default void printRunDescriptionRec(PrintWriter printWriter) {
        if (getLearnerConfig() != null) {
            getLearnerConfig().printRunDescription(printWriter);
        }
        if (getSulConfig() != null) {
            getSulConfig().printRunDescription(printWriter);
        }
    }
}
