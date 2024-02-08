package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
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

import java.time.Duration;
import java.util.List;

public class LearnerConfigTest<S, I extends MapperInput<S, I, O>, O extends AbstractOutput> {
    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new LearnerConfigStandard(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfigStandard(), null, null, null);
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
                    return new StateFuzzerClientConfigStandard(new LearnerConfigStandard(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new LearnerConfigStandard(), null, null, null);
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
                    return new StateFuzzerClientConfigEmpty(new LearnerConfigStandard(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfigStandard(), null, null, null);
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
                    return new StateFuzzerClientConfigEmpty(new LearnerConfigStandard(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new LearnerConfigStandard(), null, null, null);
                }
            }
        );
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String alphabet = "alphabetFile";
        LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.LSTAR;
        List<EquivalenceAlgorithmName> equivalenceAlgorithms = List.of(EquivalenceAlgorithmName.W_METHOD, EquivalenceAlgorithmName.WP_METHOD);
        String equivalenceAlgorithmsString = EquivalenceAlgorithmName.W_METHOD.name() + "," + EquivalenceAlgorithmName.WP_METHOD.name();
        int depth = 3;
        int minLength = 4;
        int maxLength = 5;
        int randLength = 6;
        int equivalenceQueryBound = 7;
        int memQueryRuns = 8;
        int memQueryRetries = 9;
        double probReset = 10.0;
        String testFile = "testFile";
        long seed = 11L;
        int ceReruns = 12;
        Duration timeLimit = Duration.parse("P1DT2H3M4.5S"); // 1 day, 2 hours, 3 minutes, 4.5 seconds
        Long testLimit = 13L;
        Integer roundLimit = 14;

        LearnerConfig[] learnerConfigs = parseWithStandard(stateFuzzerConfigBuilder, new String[]{
            "-alphabet", alphabet,
            "-learningAlgorithm", learningAlgorithm.name(),
            "-equivalenceAlgorithms", equivalenceAlgorithmsString,
            "-depth", String.valueOf(depth),
            "-minLength", String.valueOf(minLength),
            "-maxLength", String.valueOf(maxLength),
            "-randLength", String.valueOf(randLength),
            "-equivalenceQueryBound", String.valueOf(equivalenceQueryBound),
            "-memQueryRuns", String.valueOf(memQueryRuns),
            "-memQueryRetries", String.valueOf(memQueryRetries),
            "-logQueries",
            "-probReset", String.valueOf(probReset),
            "-testFile", testFile,
            "-seed", String.valueOf(seed),
            "-cacheTests",
            "-ceSanitizationDisable",
            "-skipNonDetTests",
            "-ceReruns", String.valueOf(ceReruns),
            "-probabilisticSanitizationDisable",
            "-timeLimit", timeLimit.toString(),
            "-testLimit", String.valueOf(testLimit),
            "-roundLimit", String.valueOf(roundLimit),
        });

        for (LearnerConfig learnerConfig : learnerConfigs) {
            Assert.assertNotNull(learnerConfig);
            Assert.assertEquals(alphabet, learnerConfig.getAlphabetFilename());
            Assert.assertEquals(learningAlgorithm, learnerConfig.getLearningAlgorithm());
            Assert.assertEquals(equivalenceAlgorithms, learnerConfig.getEquivalenceAlgorithms());
            Assert.assertEquals(depth, learnerConfig.getMaxDepth());
            Assert.assertEquals(minLength, learnerConfig.getMinLength());
            Assert.assertEquals(maxLength, learnerConfig.getMaxLength());
            Assert.assertEquals(randLength, learnerConfig.getRandLength());
            Assert.assertEquals(equivalenceQueryBound, learnerConfig.getEquivQueryBound());
            Assert.assertEquals(memQueryRuns, learnerConfig.getRunsPerMembershipQuery());
            Assert.assertEquals(memQueryRetries, learnerConfig.getMembershipQueryRetries());
            Assert.assertTrue(learnerConfig.isLogQueries());
            Assert.assertEquals(probReset, learnerConfig.getProbReset(), 0.0);
            Assert.assertEquals(testFile, learnerConfig.getTestFile());
            Assert.assertEquals(seed, learnerConfig.getSeed());
            Assert.assertTrue(learnerConfig.isCacheTests());
            Assert.assertFalse(learnerConfig.isCeSanitization());
            Assert.assertTrue(learnerConfig.isSkipNonDetTests());
            Assert.assertEquals(ceReruns, learnerConfig.getCeReruns());
            Assert.assertFalse(learnerConfig.isProbabilisticSanitization());
            Assert.assertEquals(timeLimit, learnerConfig.getTimeLimit());
            Assert.assertEquals(testLimit, learnerConfig.getTestLimit());
            Assert.assertEquals(roundLimit, learnerConfig.getRoundLimit());
        }
    }

    private LearnerConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<S, I, O> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        LearnerConfig[] learnerConfigs = new LearnerConfig[2];

        StateFuzzerClientConfig clientConfig = CommandLineParserTest.parseClientArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(clientConfig);
        learnerConfigs[0] = clientConfig.getLearnerConfig();

        StateFuzzerServerConfig serverConfig = CommandLineParserTest.parseServerArgs(commandLineParser, partialArgs);
        Assert.assertNotNull(serverConfig);
        learnerConfigs[1] = serverConfig.getLearnerConfig();

        return learnerConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new LearnerConfigEmpty(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfigEmpty(), null, null, null);
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
                    return new StateFuzzerClientConfigStandard(new LearnerConfigEmpty(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new LearnerConfigEmpty(), null, null, null);
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
                    return new StateFuzzerClientConfigEmpty(new LearnerConfigEmpty(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfigEmpty(), null, null, null);
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
                    return new StateFuzzerClientConfigEmpty(new LearnerConfigEmpty(), null, null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(new LearnerConfigEmpty(), null, null, null);
                }
            }
        );
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser<S, I, O> commandLineParser = new CommandLineParser<>(stateFuzzerConfigBuilder, null, null, null);

        String[] partialArgs = new String[] {
            "-alphabet", "alphabetPath"
        };

        CommandLineParserTest.assertInvalidClientParse(commandLineParser, partialArgs);
        CommandLineParserTest.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
