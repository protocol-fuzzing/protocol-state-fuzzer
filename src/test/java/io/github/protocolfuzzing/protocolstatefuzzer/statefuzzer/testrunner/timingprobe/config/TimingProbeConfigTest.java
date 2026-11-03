package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimingProbeConfigTest {
    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfigStandard(), null),
                () -> new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfigStandard(), null)));
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String timingProbe = "probeCommand";
        Integer probeTol = 1;
        Integer probeLow = 2;
        Integer probeHigh = 3;
        String probeExport = "probeExportFile";

        // @formatter:off
        TimingProbeConfig[] timingProbeConfigs = parseWithStandard(stateFuzzerConfigBuilder,
            new String[] {
                "-timingProbe", timingProbe,
                "-probeTol", String.valueOf(probeTol),
                "-probeLow", String.valueOf(probeLow),
                "-probeHigh", String.valueOf(probeHigh),
                "-probeExport", probeExport
            });
        // @formatter:on

        for (TimingProbeConfig timingProbeConfig: timingProbeConfigs) {
            Assertions.assertNotNull(timingProbeConfig);
            Assertions.assertEquals(timingProbe, timingProbeConfig.getProbeCmd());
            Assertions.assertEquals(probeTol, timingProbeConfig.getProbeTol());
            Assertions.assertEquals(probeLow, timingProbeConfig.getProbeLo());
            Assertions.assertEquals(probeHigh, timingProbeConfig.getProbeHi());
            Assertions.assertEquals(probeExport, timingProbeConfig.getProbeExport());
        }
    }

    private TimingProbeConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        TimingProbeConfig[] timingProbeConfigs = new TimingProbeConfig[2];

        StateFuzzerClientConfig clientConfig = CliTestSupport.parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(clientConfig);
        timingProbeConfigs[0] = clientConfig.getTimingProbeConfig();

        StateFuzzerServerConfig serverConfig = CliTestSupport.parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(serverConfig);
        timingProbeConfigs[1] = serverConfig.getTimingProbeConfig();

        return timingProbeConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfig() {}, null),
                () -> new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfig() {}, null)));
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfig() {}, null),
                () -> new StateFuzzerServerConfig() {}));
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfig() {}, null)));
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfig() {}));
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        String[] partialArgs = new String[] {"-timingProbe", "timingProbeCommand"};

        CliTestSupport.assertInvalidClientParse(commandLineParser, partialArgs);
        CliTestSupport.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
