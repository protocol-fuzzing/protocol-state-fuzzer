package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentifierConfigTest {

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
            Assertions.assertNotNull(identifierConfig);
            Assertions.assertEquals(identify, identifierConfig.getAdgPath());
            Assertions.assertEquals(adg_alphabet, identifierConfig.getAlphabetFilename());
            Assertions.assertEquals(conformance, identifierConfig.getConformance());
        }
    }

    private IdentifierConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        IdentifierConfig[] identifierConfigs = new IdentifierConfig[2];

        StateFuzzerClientConfig clientConfig = CliTestSupport.parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(clientConfig);
        identifierConfigs[0] = clientConfig.getIdentifierConfig();

        StateFuzzerServerConfig serverConfig = CliTestSupport.parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(serverConfig);
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
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        String[] partialArgs = new String[] {"-identify", "adg.dot"};

        CliTestSupport.assertInvalidClientParse(commandLineParser, partialArgs);
        CliTestSupport.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
