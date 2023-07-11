package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface of the main configuration that extends all Enablers
 * and allows state fuzzing and testing.
 */
public interface StateFuzzerConfig extends StateFuzzerEnabler, TestRunnerEnabler, TimingProbeEnabler {

    /**
     * Indicates if the help usage should be printed.
     *
     * @return  {@code true} if the help usage should be printed
     */
    boolean isHelp();

    /**
     * Indicates if the logging level should be set to DEBUG.
     *
     * @return  {@code true} if the logging level should be set to DEBUG
     */
    boolean isDebug();

    /**
     * Indicates if the logging level should be set high enough,
     * in order to let the output be quiet.
     *
     * @return  {@code true} if the logging level should be set high enough,
     *          in order to let the output be quiet
     */
    boolean isQuiet();

    /**
     * Returns the singleton PropertyResolver instance.
     *
     * @return  the singleton PropertyResolver instance
     */
    PropertyResolver getPropertyResolver();

    /**
     * Returns a unique path of a directory of the format {@code output/o_<timestamp>},
     * in which results can be saved.
     *
     * @return  a unique directory path, in which results can be saved
     */
    default String createUniqueOutputDir() {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now());
        String outputDir = "output" + File.separator + "o_" + timestamp;
        return outputDir;
    }
}
