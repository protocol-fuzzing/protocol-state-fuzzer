package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

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
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.Assert;
import org.junit.Test;

public class TestRunnerConfigTest <S, I extends MapperInput<S, I, O, P>, O extends MapperOutput<O, P>, P> {
    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, new TestRunnerConfigStandard(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, new TestRunnerConfigStandard(), null);
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCstd_SFSemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, new TestRunnerConfigStandard(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, new TestRunnerConfigStandard(), null);
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCemp_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, new TestRunnerConfigStandard(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, new TestRunnerConfigStandard(), null);
                }
            }
        );
    }

    @Test
    public void parseAllOptions_SFCemp_SFSemp() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, new TestRunnerConfigStandard(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, new TestRunnerConfigStandard(), null);
                }
            }
        );
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String test = "testFile";
        Integer times = 2;
        String testSpecification = "testSpecificationModel";

        TestRunnerConfig[] testRunnerConfigs = parseWithStandard(stateFuzzerConfigBuilder, new String[]{
            "-test", test,
            "-times", String.valueOf(times),
            "-testSpecification", testSpecification,
            "-showTransitionSequence",
        });

        for (TestRunnerConfig testRunnerConfig : testRunnerConfigs) {
            Assert.assertNotNull(testRunnerConfig);
            Assert.assertEquals(test, testRunnerConfig.getTest());
            Assert.assertEquals(times, testRunnerConfig.getTimes());
            Assert.assertEquals(testSpecification, testRunnerConfig.getTestSpecification());
            Assert.assertTrue(testRunnerConfig.isShowTransitionSequence());
        }
    }

    private TestRunnerConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<S, I, O, P> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        TestRunnerConfig[] testRunnerConfigs = new TestRunnerConfig[2];

        StateFuzzerClientConfig clientConfig = CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(clientConfig);
        testRunnerConfigs[0] = clientConfig.getTestRunnerConfig();

        StateFuzzerServerConfig serverConfig = CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(serverConfig);
        testRunnerConfigs[1] = serverConfig.getTestRunnerConfig();

        return testRunnerConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, new TestRunnerConfigEmpty(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, new TestRunnerConfigEmpty(), null);
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(null, null, new TestRunnerConfigEmpty(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, new TestRunnerConfigEmpty(), null);
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, new TestRunnerConfigEmpty(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(null, null, new TestRunnerConfigEmpty(), null);
                }
            }
        );
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, null, new TestRunnerConfigEmpty(), null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, null, new TestRunnerConfigEmpty(), null);
                }
            }
        );
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser<S, I, O, P> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        String[] partialArgs = new String[] {
            "-test", "testPath",
        };

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
