package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigEmpty;
import org.junit.Assert;
import org.junit.Test;

public class StateFuzzerServerConfigTest<M> extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        Assert.assertTrue(stateFuzzerConfig instanceof StateFuzzerServerConfigStandard);

        StateFuzzerServerConfigStandard stateFuzzerServerConfigStandard = (StateFuzzerServerConfigStandard) stateFuzzerConfig;
        Assert.assertFalse(stateFuzzerServerConfigStandard.isFuzzingClient());

        // The implementation of StateFuzzerConfigBuilder does not specify any
        // other config, so the defaults are used, which are the Empty ones
        Assert.assertTrue(stateFuzzerServerConfigStandard.getSulConfig() instanceof SulConfigEmpty);
        Assert.assertTrue(stateFuzzerServerConfigStandard.getTestRunnerConfig() instanceof TestRunnerConfigEmpty);
        Assert.assertTrue(stateFuzzerServerConfigStandard.getTimingProbeConfig() instanceof TimingProbeConfigEmpty);
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
                    return new StateFuzzerServerConfigEmpty(null);
                }
            },
            null, null, null);

        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
