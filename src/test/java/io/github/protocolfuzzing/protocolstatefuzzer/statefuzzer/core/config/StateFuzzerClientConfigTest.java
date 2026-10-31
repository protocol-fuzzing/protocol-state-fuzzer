package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateFuzzerClientConfigTest extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        StateFuzzerClientConfigStandard stateFuzzerClientConfigStandard = (StateFuzzerClientConfigStandard) stateFuzzerConfig;

        Assertions.assertTrue(stateFuzzerClientConfigStandard.isFuzzingClient());
    }

    @Override
    protected StateFuzzerClientConfig parseWithStandard(String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfigStandard(null);
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfigStandard(null);
            }
        });

        return CliTestSupport.parseClientArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfig() {};
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfigStandard(null);
            }
        });

        CliTestSupport.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
