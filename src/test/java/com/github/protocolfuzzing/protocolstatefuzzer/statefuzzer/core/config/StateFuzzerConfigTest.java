package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.time.format.DateTimeFormatter;

public abstract class StateFuzzerConfigTest {
    @Test
    public void parseDefaultOutput() {
        String prefix = "output" + File.separator + "o_";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[0]);

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assert.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    @Test
    public void parseDefaultTimestampFormat() {
        String prefix = "pre_";
        String output = prefix + "${timestamp}";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[]{
            "-output", output
        });

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assert.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    @Test
    public void parseDynamicTimestampFormat() {
        String prefix = "pre_";
        String output = prefix + "${timestamp}";
        String dateFormat = "yyyy-MM-dd";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[]{
            "-Dtimestamp.format=" + dateFormat,
            "-output", output
        });

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assert.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    protected StateFuzzerConfig parseAllOptionsWithStandard() {
        String output = "output";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[]{
            "-help",
            "-debug",
            "-quiet",
            "-output", output
        });

        Assert.assertTrue(stateFuzzerConfig.isHelp());
        Assert.assertTrue(stateFuzzerConfig.isDebug());
        Assert.assertTrue(stateFuzzerConfig.isQuiet());
        Assert.assertEquals(output, stateFuzzerConfig.getOutputDir());

        // StateFuzzerConfig constructor does not allow null configs and instantiates them
        Assert.assertNotNull(stateFuzzerConfig.getLearnerConfig());
        Assert.assertNotNull(stateFuzzerConfig.getSulConfig());
        Assert.assertNotNull(stateFuzzerConfig.getTestRunnerConfig());
        Assert.assertNotNull(stateFuzzerConfig.getTimingProbeConfig());

        return stateFuzzerConfig;
    }

    @Test
    public void invalidParseWithEmpty() {
        assertInvalidParseWithEmpty(new String[] {
            "-help",
        });
    }

    protected abstract StateFuzzerConfig parseWithStandard(String[] partialArgs);
    protected abstract void assertInvalidParseWithEmpty(String[] partialArgs);
}
