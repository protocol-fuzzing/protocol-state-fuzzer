package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.format.DateTimeFormatter;

public abstract class StateFuzzerConfigTest {
    @Test
    public void parseDefaultOutput() {
        String prefix = "output" + File.separator + "o_";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[0]);

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assertions.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    @Test
    public void parseDefaultTimestampFormat() {
        String prefix = "pre_";
        String output = prefix + "${timestamp}";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(new String[] {"-output", output});

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assertions.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    @Test
    public void parseDynamicTimestampFormat() {
        String prefix = "pre_";
        String output = prefix + "${timestamp}";
        String dateFormat = "yyyy-MM-dd";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(
            new String[] {"-Dtimestamp.format=" + dateFormat, "-output", output});

        String outputDir = stateFuzzerConfig.getOutputDir();
        Assertions.assertTrue(outputDir.startsWith(prefix));
        DateTimeFormatter.ofPattern(dateFormat).parse(outputDir.substring(prefix.length()));
    }

    protected StateFuzzerConfig parseAllOptionsWithStandard() {
        String output = "output";

        StateFuzzerConfig stateFuzzerConfig = parseWithStandard(
            new String[] {"-help", "-debug", "-quiet", "-output", output});

        Assertions.assertTrue(stateFuzzerConfig.isHelp());
        Assertions.assertTrue(stateFuzzerConfig.isDebug());
        Assertions.assertTrue(stateFuzzerConfig.isQuiet());
        Assertions.assertEquals(output, stateFuzzerConfig.getOutputDir());

        // StateFuzzerConfig constructor does not allow null configs and instantiates them
        Assertions.assertNotNull(stateFuzzerConfig.getLearnerConfig());
        Assertions.assertNotNull(stateFuzzerConfig.getSULConfig());
        Assertions.assertNotNull(stateFuzzerConfig.getTestRunnerConfig());
        Assertions.assertNotNull(stateFuzzerConfig.getTimingProbeConfig());

        return stateFuzzerConfig;
    }

    @Test
    public void invalidParseWithEmpty() {
        assertInvalidParseWithEmpty(new String[] {"-help"});
    }

    protected abstract StateFuzzerConfig parseWithStandard(String[] partialArgs);

    protected abstract void assertInvalidParseWithEmpty(String[] partialArgs);
}
