package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigEmpty;
import org.junit.Assert;
import org.junit.Test;

public class SulClientConfigTest <S, I extends MapperInput<S, I, O>, O extends MapperOutput<O>> extends SulConfigTest {
    @Test
    public void parseAllOptions_SFCstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new SulClientConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null);
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(new SulClientConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null);
                }
            }
        );
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        Long clientWait = 7L;
        Integer port = 8;
        String fuzzingRole = "client";

        SulConfig sulConfig = super.parseAllOptionsWithStandard(stateFuzzerConfigBuilder, new String[]{
            "-clientWait", String.valueOf(clientWait),
            "-port", String.valueOf(port)
        });

        Assert.assertTrue(sulConfig instanceof SulClientConfigStandard);
        SulClientConfigStandard sulClientConfigStandard = (SulClientConfigStandard) sulConfig;

        Assert.assertEquals(clientWait, sulClientConfigStandard.getClientWait());
        Assert.assertEquals(port, sulClientConfigStandard.getPort());
        Assert.assertTrue(sulClientConfigStandard.isFuzzingClient());
        Assert.assertEquals(fuzzingRole, sulClientConfigStandard.getFuzzingRole());
    }

    @Override
    protected SulClientConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<S, I, O> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        StateFuzzerClientConfig stateFuzzerClientConfig = CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);

        Assert.assertNotNull(stateFuzzerClientConfig);
        Assert.assertNotNull(stateFuzzerClientConfig);
        Assert.assertTrue(stateFuzzerClientConfig.getSulConfig() instanceof SulClientConfig);
        return (SulClientConfig) stateFuzzerClientConfig.getSulConfig();
    }

    @Test
    public void invalidParseWithEmpty_SFCstd() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new SulClientConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null);
                }
            },
            new String[]{
                "-port", "portValue"
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCemp() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(new SulClientConfigEmpty());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null);
                }
            },
            new String[]{
                "-port", "portValue"
            }
        );
    }

    @Override
    protected void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<S, I, O> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);
        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
    }
}
