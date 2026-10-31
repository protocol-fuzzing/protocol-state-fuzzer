package io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.DiffTesterConfigBuilderSimple;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.FingerprintConfigBuilderSimple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SULClientConfigTest<M> extends SULConfigTest {
    @Test
    public void parseAllOptions_SFCstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new SULClientConfigStandard());
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            });
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        Long clientWait = 7L;
        Integer port = 8;
        String fuzzingRole = "client";

        SULConfig sulConfig = super.parseAllOptionsWithStandard(stateFuzzerConfigBuilder,
            new String[] {"-clientWait", String.valueOf(clientWait), "-port", String.valueOf(port)});

        Assertions.assertTrue(sulConfig instanceof SULClientConfigStandard);
        SULClientConfigStandard sulClientConfigStandard = (SULClientConfigStandard) sulConfig;

        Assertions.assertEquals(clientWait, sulClientConfigStandard.getClientWait());
        Assertions.assertEquals(port, sulClientConfigStandard.getPort());
        Assertions.assertTrue(sulClientConfigStandard.isFuzzingClient());
        Assertions.assertEquals(fuzzingRole, sulClientConfigStandard.getFuzzingRole());
    }

    @Override
    protected SULClientConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(), null, null, null, null, null,
            null);

        StateFuzzerClientConfig stateFuzzerClientConfig = CommandLineParserTest.parseClientArgs(commandLineParser,
            partialArgs);

        Assertions.assertNotNull(stateFuzzerClientConfig);
        Assertions.assertNotNull(stateFuzzerClientConfig);
        Assertions.assertTrue(stateFuzzerClientConfig.getSULConfig() instanceof SULClientConfig);
        return (SULClientConfig) stateFuzzerClientConfig.getSULConfig();
    }

    @Test
    public void invalidParseWithEmpty_SFCstd() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new SULClientConfig() {});
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            },
            new String[] {"-port", "portValue"});
    }

    @Test
    public void invalidParseWithEmpty_SFCemp() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            },
            new String[] {"-port", "portValue"});
    }

    @Override
    protected void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(), null, null, null, null, null,
            null);
        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
