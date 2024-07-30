package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigEmpty;
import org.junit.Assert;
import org.junit.Test;

public class StateFuzzerClientConfigTest extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        Assert.assertTrue(stateFuzzerConfig instanceof StateFuzzerClientConfigStandard);
        StateFuzzerClientConfigStandard stateFuzzerClientConfigStandard = (StateFuzzerClientConfigStandard) stateFuzzerConfig;

        Assert.assertTrue(stateFuzzerClientConfigStandard.isFuzzingClient());

        // The implementation of StateFuzzerConfigBuilder does not specify any
        // other config, so the defaults are used, which are the Empty ones
        Assert.assertTrue(stateFuzzerClientConfigStandard.getLearnerConfig() instanceof LearnerConfigEmpty);
        Assert.assertTrue(stateFuzzerClientConfigStandard.getSulConfig() instanceof SulConfigEmpty);
        Assert.assertTrue(stateFuzzerClientConfigStandard.getTestRunnerConfig() instanceof TestRunnerConfigEmpty);
        Assert.assertTrue(stateFuzzerClientConfigStandard.getTimingProbeConfig() instanceof TimingProbeConfigEmpty);
    }

    @Override
    protected StateFuzzerClientConfig parseWithStandard(String[] partialArgs) {
        CommandLineParser commandLineParser = new CommandLineParser(
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
            null, null, null, null);

        return CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser commandLineParser = new CommandLineParser(
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
            null, null, null, null);

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
