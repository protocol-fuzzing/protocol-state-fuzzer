package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.Assert;
import org.junit.Test;

public class SulServerConfigTest<M> extends SulConfigTest {
    @Test
    public void parseAllOptions_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new SulServerConfigStandard());
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFSemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new SulServerConfigStandard());
                }
            }
        );
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String connect = "host:1234";
        String fuzzingRole = "server";

        SulConfig sulConfig = super.parseAllOptionsWithStandard(stateFuzzerConfigBuilder, new String[]{
                "-connect", connect
        });

        Assert.assertTrue(sulConfig instanceof SulServerConfigStandard);
        SulServerConfigStandard sulServerConfigStandard = (SulServerConfigStandard) sulConfig;

        Assert.assertEquals(connect, sulServerConfigStandard.getHost());
        Assert.assertFalse(sulServerConfigStandard.isFuzzingClient());
        Assert.assertEquals(fuzzingRole, sulServerConfigStandard.getFuzzingRole());
    }

    @Override
    protected SulServerConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        StateFuzzerServerConfig stateFuzzerServerConfig = CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);

        Assert.assertNotNull(stateFuzzerServerConfig);
        Assert.assertNotNull(stateFuzzerServerConfig);
        Assert.assertTrue(stateFuzzerServerConfig.getSulConfig() instanceof SulServerConfig);
        return (SulServerConfig) stateFuzzerServerConfig.getSulConfig();
    }

    @Test
    public void invalidParseWithEmpty_SFSstd() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new SulServerConfig(){});
                }
            },
            new String[]{
                "-connect", "connectString"
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFSemp() {
        super.invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new SulServerConfig(){});
                }
            },
            new String[]{
                "-connect", "connectString"
            }
        );
    }
    @Override
    protected void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
