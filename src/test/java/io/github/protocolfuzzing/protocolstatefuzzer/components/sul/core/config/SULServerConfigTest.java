package io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SULServerConfigTest extends SULConfigTest {
    @Test
    public void parseAllOptions_SFSstd() {
        parseAllOptions(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfigStandard(new SULServerConfigStandard())));
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String connect = "host:1234";
        String fuzzingRole = "server";

        SULConfig sulConfig = super.parseAllOptionsWithStandard(stateFuzzerConfigBuilder,
            new String[] {"-connect", connect});

        Assertions.assertTrue(sulConfig instanceof SULServerConfigStandard);
        SULServerConfigStandard sulServerConfigStandard = (SULServerConfigStandard) sulConfig;

        Assertions.assertEquals(connect, sulServerConfigStandard.getHost());
        Assertions.assertFalse(sulServerConfigStandard.isFuzzingClient());
        Assertions.assertEquals(fuzzingRole, sulServerConfigStandard.getFuzzingRole());
    }

    @Override
    protected SULServerConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        StateFuzzerServerConfig stateFuzzerServerConfig = CliTestSupport.parseServerArgs(commandLineParser,
            partialArgs);

        Assertions.assertNotNull(stateFuzzerServerConfig);
        Assertions.assertNotNull(stateFuzzerServerConfig);
        Assertions.assertTrue(stateFuzzerServerConfig.getSULConfig() instanceof SULServerConfig);
        return (SULServerConfig) stateFuzzerServerConfig.getSULConfig();
    }

    @Test
    public void invalidParseWithEmpty_SFSstd() {
        super.invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfigStandard(new SULServerConfig() {})),
            new String[] {"-connect", "connectString"});
    }

    @Test
    public void invalidParseWithEmpty_SFSemp() {
        super.invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfig() {}),
            new String[] {"-connect", "connectString"});
    }

    @Override
    protected void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);
        CliTestSupport.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
