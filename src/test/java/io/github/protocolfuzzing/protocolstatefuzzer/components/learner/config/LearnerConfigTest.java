package io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

public class LearnerConfigTest {
    @Test
    public void parseAllOptions_SFCstd_SFSstd() {
        parseAllOptions(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new LearnerConfigStandard(), null, null, null, null);
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfigStandard(), null, null, null, null);
                }
            });
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        String alphabet = "alphabetFile";
        LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.LSTAR;
        List<EquivalenceAlgorithmName> equivalenceAlgorithms = List.of(EquivalenceAlgorithmName.W_METHOD,
            EquivalenceAlgorithmName.WP_METHOD);
        String equivalenceAlgorithmsString = EquivalenceAlgorithmName.W_METHOD.name() + ","
            + EquivalenceAlgorithmName.WP_METHOD.name();
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

        // @formatter:off
        LearnerConfig[] learnerConfigs = parseWithStandard(stateFuzzerConfigBuilder,
            new String[] {
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
                "-logQueries", "-probReset", String.valueOf(probReset),
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
        // @formatter:on

        for (LearnerConfig learnerConfig: learnerConfigs) {
            Assertions.assertNotNull(learnerConfig);
            Assertions.assertEquals(alphabet, learnerConfig.getAlphabetFilename());
            Assertions.assertEquals(learningAlgorithm, learnerConfig.getLearningAlgorithm());
            Assertions.assertEquals(equivalenceAlgorithms, learnerConfig.getEquivalenceAlgorithms());
            Assertions.assertEquals(depth, learnerConfig.getMaxDepth());
            Assertions.assertEquals(minLength, learnerConfig.getMinLength());
            Assertions.assertEquals(maxLength, learnerConfig.getMaxLength());
            Assertions.assertEquals(randLength, learnerConfig.getRandLength());
            Assertions.assertEquals(equivalenceQueryBound, learnerConfig.getEquivQueryBound());
            Assertions.assertEquals(memQueryRuns, learnerConfig.getRunsPerMembershipQuery());
            Assertions.assertEquals(memQueryRetries, learnerConfig.getMembershipQueryRetries());
            Assertions.assertTrue(learnerConfig.isLogQueries());
            Assertions.assertEquals(probReset, learnerConfig.getProbReset(), 0.0);
            Assertions.assertEquals(testFile, learnerConfig.getTestFile());
            Assertions.assertEquals(seed, learnerConfig.getSeed());
            Assertions.assertTrue(learnerConfig.isCacheTests());
            Assertions.assertFalse(learnerConfig.isCeSanitization());
            Assertions.assertTrue(learnerConfig.isSkipNonDetTests());
            Assertions.assertEquals(ceReruns, learnerConfig.getCeReruns());
            Assertions.assertFalse(learnerConfig.isProbabilisticSanitization());
            Assertions.assertEquals(timeLimit, learnerConfig.getTimeLimit());
            Assertions.assertEquals(testLimit, learnerConfig.getTestLimit());
            Assertions.assertEquals(roundLimit, learnerConfig.getRoundLimit());
        }
    }

    private LearnerConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        LearnerConfig[] learnerConfigs = new LearnerConfig[2];

        StateFuzzerClientConfig clientConfig = CliTestSupport.parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(clientConfig);
        learnerConfigs[0] = clientConfig.getLearnerConfig();

        StateFuzzerServerConfig serverConfig = CliTestSupport.parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertNotNull(serverConfig);
        learnerConfigs[1] = serverConfig.getLearnerConfig();

        return learnerConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new LearnerConfig() {}, null, null, null, null);
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfig() {}, null, null, null, null);
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new LearnerConfig() {}, null, null, null, null);
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new LearnerConfig() {}, null, null, null, null);
                }
            });
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig() {};
                }

                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig() {};
                }
            });
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        String[] partialArgs = new String[] {"-alphabet", "alphabetPath"};

        CliTestSupport.assertInvalidClientParse(commandLineParser, partialArgs);
        CliTestSupport.assertInvalidServerParse(commandLineParser, partialArgs);
    }
}
