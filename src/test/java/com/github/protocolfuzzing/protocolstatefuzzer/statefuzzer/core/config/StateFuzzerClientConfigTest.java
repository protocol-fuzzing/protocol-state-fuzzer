package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import org.junit.Assert;
import org.junit.Test;

public class StateFuzzerClientConfigTest<M> extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        Assert.assertTrue(stateFuzzerConfig instanceof StateFuzzerClientConfigStandard);
        StateFuzzerClientConfigStandard stateFuzzerClientConfigStandard = (StateFuzzerClientConfigStandard) stateFuzzerConfig;

        Assert.assertTrue(stateFuzzerClientConfigStandard.isFuzzingClient());
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
            null, null, null);

        return CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null);
                }
            },
            null, null, null);

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
