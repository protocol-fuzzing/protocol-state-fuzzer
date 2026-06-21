package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.DiffTesterConfigBuilderSimple;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.FingerprintConfigBuilderSimple;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierConfigStandard;
import org.junit.Assert;
import org.junit.Test;

public class SulIdentifierConfigTest<M> {

    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, null, new IdentifierConfigStandard());
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, null, new IdentifierConfigStandard());
                }
            });
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String identify = "testFile";
        String adg_alphabet = "alphabet";
        String conformance = "conformanceModels";

        // @formatter:off
        IdentifierConfig[] identifierConfigs = parseWithStandard(stateFuzzerConfigBuilder,
            new String[] {
                "-identify", identify,
                "-adg_alphabet", adg_alphabet,
                "-conformance", conformance,
            });
        // @formatter:on

        for (IdentifierConfig identifierConfig: identifierConfigs) {
            Assert.assertNotNull(identifierConfig);
            Assert.assertEquals(identify, identifierConfig.getAdgPath());
            Assert.assertEquals(adg_alphabet, identifierConfig.getAlphabetFilename());
            Assert.assertEquals(conformance, identifierConfig.getConformance());
        }
    }

    private IdentifierConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(),
            null, null, null, null, null, null);

        IdentifierConfig[] identifierConfigs = new IdentifierConfig[2];

        StateFuzzerClientConfig clientConfig = CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(clientConfig);
        identifierConfigs[0] = clientConfig.getIdentifierConfig();

        StateFuzzerServerConfig serverConfig = CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(serverConfig);
        identifierConfigs[1] = serverConfig.getIdentifierConfig();

        return identifierConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, null, new IdentifierConfig() {});
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, null, new IdentifierConfig() {});
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, null, null, new IdentifierConfig() {});
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, null, null, new IdentifierConfig() {});
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            });
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(),
            null, null, null, null, null, null);

        String[] partialArgs = new String[] {"-identify", "adg.dot"};

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
