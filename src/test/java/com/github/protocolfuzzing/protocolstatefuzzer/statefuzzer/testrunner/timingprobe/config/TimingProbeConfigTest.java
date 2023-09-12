package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.Assert;
import org.junit.Test;

public class TimingProbeConfigTest {
    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfigStandard());
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCstd_SFSemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, null, new TimingProbeConfigStandard());
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCemp_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, null, new TimingProbeConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfigStandard());
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCemp_SFSemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, null, new TimingProbeConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, null, new TimingProbeConfigStandard());
                }
            }
        );
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String timingProbe = "probeCommand";
        Integer probeMin = 1;
        Integer probeLow = 2;
        Integer probeHigh = 3;
        String probeExport = "probeExportFile";

        TimingProbeConfig[] timingProbeConfigs = parseWithStandard(stateFuzzerConfigBuilder, new String[]{
            "-timingProbe", timingProbe,
            "-probeMin", String.valueOf(probeMin),
            "-probeLow", String.valueOf(probeLow),
            "-probeHigh", String.valueOf(probeHigh),
            "-probeExport", probeExport,
        });

        for (TimingProbeConfig timingProbeConfig : timingProbeConfigs) {
            Assert.assertNotNull(timingProbeConfig);
            Assert.assertEquals(timingProbe, timingProbeConfig.getProbeCmd());
            Assert.assertEquals(probeMin, timingProbeConfig.getProbeMin());
            Assert.assertEquals(probeLow, timingProbeConfig.getProbeLo());
            Assert.assertEquals(probeHigh, timingProbeConfig.getProbeHi());
            Assert.assertEquals(probeExport, timingProbeConfig.getProbeExport());
        }
    }

    private TimingProbeConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser commandLineParser = new CommandLineParser(stateFuzzerConfigBuilder, null, null, null);

        TimingProbeConfig[] timingProbeConfigs = new TimingProbeConfig[2];

        StateFuzzerClientConfig clientConfig = CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(clientConfig);
        timingProbeConfigs[0] = clientConfig.getTimingProbeConfig();

        StateFuzzerServerConfig serverConfig = CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(serverConfig);
        timingProbeConfigs[1] = serverConfig.getTimingProbeConfig();

        return timingProbeConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfigEmpty());
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, new TimingProbeConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, null, new TimingProbeConfigEmpty());
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, null, new TimingProbeConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, new TimingProbeConfigEmpty());
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, null, new TimingProbeConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, null, new TimingProbeConfigEmpty());
                }
            }
        );
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser commandLineParser = new CommandLineParser(stateFuzzerConfigBuilder, null, null, null);

        String[] partialArgs = new String[] {
            "-timingProbe", "timingProbeCommand"
        };

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
