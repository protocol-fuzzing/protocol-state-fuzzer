package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateFuzzerServerConfigTest extends StateFuzzerConfigTest {
    @Test
    public void parseAllOptions() {
        StateFuzzerConfig stateFuzzerConfig = super.parseAllOptionsWithStandard();

        StateFuzzerServerConfigStandard stateFuzzerServerConfigStandard = (StateFuzzerServerConfigStandard) stateFuzzerConfig;
        Assertions.assertFalse(stateFuzzerServerConfigStandard.isFuzzingClient());
    }

    @Override
    protected StateFuzzerServerConfig parseWithStandard(String[] partialArgs) {
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

        return CliTestSupport.parseServerArgs(commandLineParser, partialArgs);
    }

    @Override
    protected void assertInvalidParseWithEmpty(String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfigStandard(null);
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfig() {};
            }
        });

        CliTestSupport.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
