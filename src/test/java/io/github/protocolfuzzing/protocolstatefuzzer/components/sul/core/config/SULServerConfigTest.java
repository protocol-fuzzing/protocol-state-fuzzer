package io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.DiffTesterConfigBuilderSimple;
import org.junit.Assert;
import org.junit.Test;

public class SULServerConfigTest<M> extends SULConfigTest {
    @Test
    public void parseAllOptions_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new SULServerConfigStandard());
                }
            });
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String connect = "host:1234";
        String fuzzingRole = "server";

        SULConfig sulConfig = super.parseAllOptionsWithStandard(stateFuzzerConfigBuilder,
            new String[] {"-connect", connect});

        Assert.assertTrue(sulConfig instanceof SULServerConfigStandard);
        SULServerConfigStandard sulServerConfigStandard = (SULServerConfigStandard) sulConfig;

        Assert.assertEquals(connect, sulServerConfigStandard.getHost());
        Assert.assertFalse(sulServerConfigStandard.isFuzzingClient());
        Assert.assertEquals(fuzzingRole, sulServerConfigStandard.getFuzzingRole());
    }

    @Override
    protected SULServerConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), null, null, null, null);

        StateFuzzerServerConfig stateFuzzerServerConfig = CommandLineParserTest.parseServerArgs(commandLineParser,
            partialArgs);

        Assert.assertNotNull(stateFuzzerServerConfig);
        Assert.assertNotNull(stateFuzzerServerConfig);
        Assert.assertTrue(stateFuzzerServerConfig.getSULConfig() instanceof SULServerConfig);
        return (SULServerConfig) stateFuzzerServerConfig.getSULConfig();
    }

    @Test
    public void invalidParseWithEmpty_SFSstd() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new SULServerConfig() {});
                }
            },
            new String[] {"-connect", "connectString"});
    }

    @Test
    public void invalidParseWithEmpty_SFSemp() {
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
            new String[] {"-connect", "connectString"});
    }

    @Override
    protected void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder,
            new DiffTesterConfigBuilderSimple(), null, null, null, null);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
