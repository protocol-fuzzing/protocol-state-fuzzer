package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import org.junit.Assert;
import org.junit.Test;

public class StateFuzzerServerConfigTest<M> extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        Assert.assertTrue(stateFuzzerConfig instanceof StateFuzzerServerConfigStandard);

        StateFuzzerServerConfigStandard stateFuzzerServerConfigStandard = (StateFuzzerServerConfigStandard) stateFuzzerConfig;
        Assert.assertFalse(stateFuzzerServerConfigStandard.isFuzzingClient());
    }

    @Override
    protected StateFuzzerServerConfig parseWithStandard(String[] partialArgs) {
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

        return CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig(){};
                }
            },
            null, null, null);

        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
