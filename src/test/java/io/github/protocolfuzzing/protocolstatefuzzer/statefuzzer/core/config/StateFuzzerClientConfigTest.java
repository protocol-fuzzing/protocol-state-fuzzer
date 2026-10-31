package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.DiffTesterConfigBuilderSimple;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.FingerprintConfigBuilderSimple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateFuzzerClientConfigTest<M> extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        StateFuzzerClientConfigStandard stateFuzzerClientConfigStandard = (StateFuzzerClientConfigStandard) stateFuzzerConfig;

        Assertions.assertTrue(stateFuzzerClientConfigStandard.isFuzzingClient());
    }

    @Override
    protected StateFuzzerClientConfig parseWithStandard(String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null);
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null);
                }
            },
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(),
            null, null, null, null, null, null);

        return CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null);
                }
            },
            new DiffTesterConfigBuilderSimple(), new FingerprintConfigBuilderSimple(),
            null, null, null, null, null, null);

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
