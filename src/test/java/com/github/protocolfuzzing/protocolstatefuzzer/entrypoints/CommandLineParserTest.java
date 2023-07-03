package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.EquivalenceAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningAlgorithmName;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.InputResponseTimeoutMap;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class CommandLineParserTest {
    @Test
    public void parseAllOptionsOfStateFuzzerClientConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String output = "output";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                "-help",
                "-debug",
                "-quiet",
                // StateFuzzerConfig options
                "-output", output,
                // SulClientConfig required options not asserted here
                "-port", "0"
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        Assert.assertTrue(stateFuzzerClientConfig.isHelp());
        Assert.assertTrue(stateFuzzerClientConfig.isDebug());
        Assert.assertTrue(stateFuzzerClientConfig.isQuiet());
        Assert.assertEquals(output, stateFuzzerClientConfig.getOutputDir());
        Assert.assertTrue(stateFuzzerClientConfig.isFuzzingClient());

        // StateFuzzerConfig constructor does not allow null configs and instantiates them
        // The same applies for the SulConfig constructor with MapperConfig and SulAdapterConfig
        // SulClientConfig cannot be instantiated, as an abstract class, thus a subclass
        // implementing it should be provided
        Assert.assertNotNull(stateFuzzerClientConfig.getLearnerConfig());
        Assert.assertNotNull(stateFuzzerClientConfig.getSulConfig());
        Assert.assertNotNull(stateFuzzerClientConfig.getSulConfig().getMapperConfig());
        Assert.assertNotNull(stateFuzzerClientConfig.getSulConfig().getSulAdapterConfig());
        Assert.assertNotNull(stateFuzzerClientConfig.getTestRunnerConfig());
        Assert.assertNotNull(stateFuzzerClientConfig.getTimingProbeConfig());
    }

    @Test
    public void parseAllOptionsOfStateFuzzerServerConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String output = "output";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                "-help",
                "-debug",
                "-quiet",
                // StateFuzzerConfig options
                "-output", output,
                // SulServerConfig required options not asserted here
                "-connect", "host:1234"
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        Assert.assertTrue(stateFuzzerServerConfig.isHelp());
        Assert.assertTrue(stateFuzzerServerConfig.isDebug());
        Assert.assertTrue(stateFuzzerServerConfig.isQuiet());
        Assert.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
        Assert.assertFalse(stateFuzzerServerConfig.isFuzzingClient());

        // StateFuzzerConfig constructor does not allow null configs and instantiates them
        // The same applies for the SulConfig constructor with MapperConfig and SulAdapterConfig
        // SulServerConfig cannot be instantiated, as an abstract class, thus a subclass
        // implementing it should be provided
        Assert.assertNotNull(stateFuzzerServerConfig.getLearnerConfig());
        Assert.assertNotNull(stateFuzzerServerConfig.getSulConfig());
        Assert.assertNotNull(stateFuzzerServerConfig.getSulConfig().getMapperConfig());
        Assert.assertNotNull(stateFuzzerServerConfig.getSulConfig().getSulAdapterConfig());
        Assert.assertNotNull(stateFuzzerServerConfig.getTestRunnerConfig());
        Assert.assertNotNull(stateFuzzerServerConfig.getTimingProbeConfig());
    }

    @Test
    public void parseAllOptionsOfSulClientConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        Long responseWait = 1L;
        InputResponseTimeoutMap inputResponseTimeoutMap = new InputResponseTimeoutMap();
        inputResponseTimeoutMap.put("IN_2", 2L);
        inputResponseTimeoutMap.put("IN_3", 3L);
        String inputResponseTimeoutString = "IN_2:2,IN_3:3";
        String sulCommand = "sulCommand";
        String terminateCommand = "terminateCommand";
        String processDir = "processDir";
        ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;
        Long startWait = 4L;
        long clientWait = 7;
        int port = 8;
        String fuzzingRole = "client";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                // SulConfig options
                "-responseWait", String.valueOf(responseWait),
                "-inputResponseTimeout", inputResponseTimeoutString,
                "-command", sulCommand,
                "-terminateCommand", terminateCommand,
                "-processDir", processDir,
                "-redirectOutputStreams",
                "-processTrigger", processTrigger.name(),
                "-startWait", String.valueOf(startWait),
                // SulClientConfig options
                "-clientWait", String.valueOf(clientWait),
                "-port", String.valueOf(port)
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        Assert.assertTrue(stateFuzzerClientConfig.getSulConfig() instanceof SulClientConfig);
        SulClientConfig sulClientConfig = (SulClientConfig) stateFuzzerClientConfig.getSulConfig();

        Assert.assertNotNull(sulClientConfig);
        Assert.assertNotNull(sulClientConfig.getMapperConfig());
        Assert.assertEquals(responseWait, sulClientConfig.getResponseWait());
        Assert.assertEquals(inputResponseTimeoutMap, sulClientConfig.getInputResponseTimeout());
        Assert.assertEquals(sulCommand, sulClientConfig.getCommand());
        Assert.assertEquals(terminateCommand, sulClientConfig.getTerminateCommand());
        Assert.assertEquals(processDir, sulClientConfig.getProcessDir());
        Assert.assertTrue(sulClientConfig.isRedirectOutputStreams());
        Assert.assertEquals(processTrigger, sulClientConfig.getProcessTrigger());
        Assert.assertEquals(startWait, sulClientConfig.getStartWait());

        Assert.assertEquals(clientWait, sulClientConfig.getClientWait());
        Assert.assertEquals(port, sulClientConfig.getPort());
        Assert.assertTrue(sulClientConfig.isFuzzingClient());
        Assert.assertEquals(fuzzingRole, sulClientConfig.getFuzzingRole());
    }

    @Test
    public void parseAllOptionsOfSulServerConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        Long responseWait = 1L;
        InputResponseTimeoutMap inputResponseTimeoutMap = new InputResponseTimeoutMap();
        inputResponseTimeoutMap.put("IN_2", 2L);
        inputResponseTimeoutMap.put("IN_3", 3L);
        String inputResponseTimeoutString = "IN_2:2,IN_3:3";
        String sulCommand = "sulCommand";
        String terminateCommand = "terminateCommand";
        String processDir = "processDir";
        ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;
        Long startWait = 4L;
        String connect = "host:1234";
        String fuzzingRole = "server";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // SulConfig options
                "-responseWait", String.valueOf(responseWait),
                "-inputResponseTimeout", inputResponseTimeoutString,
                "-command", sulCommand,
                "-terminateCommand", terminateCommand,
                "-processDir", processDir,
                "-redirectOutputStreams",
                "-processTrigger", processTrigger.name(),
                "-startWait", String.valueOf(startWait),
                // SulServerConfig options
                "-connect", connect
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        Assert.assertTrue(stateFuzzerServerConfig.getSulConfig() instanceof SulServerConfig);
        SulServerConfig sulServerConfig = (SulServerConfig) stateFuzzerServerConfig.getSulConfig();

        Assert.assertNotNull(sulServerConfig);
        Assert.assertNotNull(sulServerConfig.getMapperConfig());
        Assert.assertEquals(responseWait, sulServerConfig.getResponseWait());
        Assert.assertEquals(inputResponseTimeoutMap, sulServerConfig.getInputResponseTimeout());
        Assert.assertEquals(sulCommand, sulServerConfig.getCommand());
        Assert.assertEquals(terminateCommand, sulServerConfig.getTerminateCommand());
        Assert.assertEquals(processDir, sulServerConfig.getProcessDir());
        Assert.assertTrue(sulServerConfig.isRedirectOutputStreams());
        Assert.assertEquals(processTrigger, sulServerConfig.getProcessTrigger());
        Assert.assertEquals(startWait, sulServerConfig.getStartWait());

        Assert.assertEquals(connect, sulServerConfig.getHost());
        Assert.assertFalse(sulServerConfig.isFuzzingClient());
        Assert.assertEquals(fuzzingRole, sulServerConfig.getFuzzingRole());
    }

    @Test
    public void parseAllOptionsOfLearnerConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String alphabet = "alphabetFile";
        LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.LSTAR;
        List<EquivalenceAlgorithmName> equivalenceAlgorithms = Arrays.asList(EquivalenceAlgorithmName.W_METHOD, EquivalenceAlgorithmName.WP_METHOD);
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
        long seed = 11;
        int ceReruns = 12;
        Duration timeLimit = Duration.parse("P1DT2H3M4.5S"); // 1 day, 2 hours, 3 minutes, 4.5 seconds
        Long testLimit = 13L;
        Integer roundLimit = 14;

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                // LearnerConfig options
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
                // SulClientConfig required options not asserted here
                "-port", "0",
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        LearnerConfig learnerConfig = stateFuzzerClientConfig.getLearnerConfig();

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
        Assert.assertEquals(seed, learnerConfig.getSeed(), 0.0);
        Assert.assertTrue(learnerConfig.isCacheTests());
        Assert.assertFalse(learnerConfig.isCeSanitization());
        Assert.assertTrue(learnerConfig.isSkipNonDetTests());
        Assert.assertEquals(ceReruns, learnerConfig.getCeReruns());
        Assert.assertFalse(learnerConfig.isProbabilisticSanitization());
        Assert.assertEquals(timeLimit, learnerConfig.getTimeLimit());
        Assert.assertEquals(testLimit, learnerConfig.getTestLimit());
        Assert.assertEquals(roundLimit, learnerConfig.getRoundLimit());
    }

    @Test
    public void parseAllOptionsOfMapperConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String mapperConnectionConfig = "mapperConnectionConfigFile";
        List<String> repeatingOutputs = Arrays.asList("OUT_1", "OUT_2");

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // MapperConfig options
                "-mapperConnectionConfig", mapperConnectionConfig,
                "-repeatingOutputs", String.join(",", repeatingOutputs),
                "-socketClosedAsTimeout",
                "-disabledAsTimeout",
                "-dontMergeRepeating",
                // SulServerConfig required options not asserted here
                "-connect", "host:1234",
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        Assert.assertNotNull(stateFuzzerServerConfig.getSulConfig());
        MapperConfig mapperConfig = stateFuzzerServerConfig.getSulConfig().getMapperConfig();

        Assert.assertNotNull(mapperConfig);
        Assert.assertEquals(mapperConnectionConfig, mapperConfig.getMapperConnectionConfig());
        Assert.assertEquals(repeatingOutputs, mapperConfig.getRepeatingOutputs());
        Assert.assertTrue(mapperConfig.isSocketClosedAsTimeout());
        Assert.assertTrue(mapperConfig.isDisabledAsTimeout());
        Assert.assertFalse(mapperConfig.isMergeRepeating());
    }

    @Test
    public void parseAllOptionsOfSulAdapterConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        Integer adapterPort = 1;
        String adapterAddress = "adapterAddress";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // SulAdapterConfig options
                "-adapterPort", String.valueOf(adapterPort),
                "-adapterAddress", adapterAddress,
                // SulServerConfig required options not asserted here
                "-connect", "host:1234",
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        Assert.assertNotNull(stateFuzzerServerConfig.getSulConfig());
        SulAdapterConfig sulAdapterConfig = stateFuzzerServerConfig.getSulConfig().getSulAdapterConfig();

        Assert.assertNotNull(sulAdapterConfig);
        Assert.assertEquals(adapterPort, sulAdapterConfig.getAdapterPort());
        Assert.assertEquals(adapterAddress, sulAdapterConfig.getAdapterAddress());
    }

    @Test
    public void parseAllOptionsOfTestRunnerConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String test = "testFile";
        Integer times = 2;
        String testSpecification = "testSpecificationModel";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                // TestRunnerConfig options
                "-test", test,
                "-times", String.valueOf(times),
                "-testSpecification", testSpecification,
                "-showTransitionSequence",
                // SulClientConfig required options not asserted here
                "-port", "0",
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        TestRunnerConfig testRunnerConfig = stateFuzzerClientConfig.getTestRunnerConfig();

        Assert.assertNotNull(testRunnerConfig);
        Assert.assertEquals(test, testRunnerConfig.getTest());
        Assert.assertEquals(times, testRunnerConfig.getTimes());
        Assert.assertEquals(testSpecification, testRunnerConfig.getTestSpecification());
        Assert.assertTrue(testRunnerConfig.isShowTransitionSequence());
    }

    @Test
    public void parseAllOptionsOfTimingProbeConfig() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String timingProbe = "probeCommand";
        Integer probeMin = 1;
        Integer probeLow = 2;
        Integer probeHigh = 3;
        String probeExport = "probeExportFile";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // TimingProbeConfig options
                "-timingProbe", timingProbe,
                "-probeMin", String.valueOf(probeMin),
                "-probeLow", String.valueOf(probeLow),
                "-probeHigh", String.valueOf(probeHigh),
                "-probeExport", probeExport,
                // SulServerConfig required options not asserted here
                "-connect", "host:1234",
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        TimingProbeConfig timingProbeConfig = stateFuzzerServerConfig.getTimingProbeConfig();

        Assert.assertNotNull(timingProbeConfig);
        Assert.assertEquals(timingProbe, timingProbeConfig.getProbeCmd());
        Assert.assertEquals(probeMin, timingProbeConfig.getProbeMin());
        Assert.assertEquals(probeLow, timingProbeConfig.getProbeLo());
        Assert.assertEquals(probeHigh, timingProbeConfig.getProbeHi());
        Assert.assertEquals(probeExport, timingProbeConfig.getProbeExport());
    }

    @Test
    public void parseDynamicOptionsBeforeUsage() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        int port = 1234;

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                // dynamic options before usage
                "-Dsul.port=1",
                "-DportValue=34",
                // SulClientConfig required options
                "-port", "${sul.port}2${portValue}",
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        Assert.assertTrue(stateFuzzerClientConfig.getSulConfig() instanceof SulClientConfig);
        SulClientConfig sulClientConfig = (SulClientConfig) stateFuzzerClientConfig.getSulConfig();

        Assert.assertNotNull(sulClientConfig);
        Assert.assertEquals(port, sulClientConfig.getPort());
    }

    @Test
    public void parseDynamicOptionsAfterUsage() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String connect = "host:1234";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // SulServerConfig required options
                "-connect", "host:${sul.port}2${portValue}",
                // dynamic options after usage
                "-Dsul.port=1",
                "-DportValue=34",
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        Assert.assertTrue(stateFuzzerServerConfig.getSulConfig() instanceof SulServerConfig);
        SulServerConfig sulServerConfig = (SulServerConfig) stateFuzzerServerConfig.getSulConfig();

        Assert.assertNotNull(sulServerConfig);
        Assert.assertEquals(connect, sulServerConfig.getHost());
    }

    @Test
    public void parseDynamicOptionsBeforeAndAfterUsage() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        int port = 1234;

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                // dynamic options before usage
                "-Dsul.port=1",
                // SulClientConfig required options
                "-port", "${sul.port}2${portValue}",
                // dynamic options after usage
                "-DportValue=34",
        });
        StateFuzzerClientConfig stateFuzzerClientConfig = assertParseResultOfClient(parseResult);

        Assert.assertTrue(stateFuzzerClientConfig.getSulConfig() instanceof SulClientConfig);
        SulClientConfig sulClientConfig = (SulClientConfig) stateFuzzerClientConfig.getSulConfig();

        Assert.assertNotNull(sulClientConfig);
        Assert.assertEquals(port, sulClientConfig.getPort());
    }

    @Test
    public void parseDynamicExplicitConverterOptionsAfterUsage() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        Duration timeLimit = Duration.parse("P1DT2H3M4.5S"); // 1 day, 2 hours, 3 minutes, 4.5 seconds
        InputResponseTimeoutMap inputResponseTimeoutMap = new InputResponseTimeoutMap();
        inputResponseTimeoutMap.put("IN_2", 2L);
        inputResponseTimeoutMap.put("IN_3", 3L);
        String inputResponseTimeoutString = "IN_2:2,IN_3:3";
        String connect = "host:1234";

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                // LearnerConfig options with explicit converter
                "-timeLimit", timeLimit.toString(),
                // SulConfig options with explicit converter
                "-inputResponseTimeout", inputResponseTimeoutString,
                // SulServerConfig required options
                "-connect", "host:${sul.port}2${portValue}",
                // dynamic options after usage
                "-Dsul.port=1",
                "-DportValue=34",
        });
        StateFuzzerServerConfig stateFuzzerServerConfig = assertParseResultOfServer(parseResult);

        LearnerConfig learnerConfig = stateFuzzerServerConfig.getLearnerConfig();

        Assert.assertTrue(stateFuzzerServerConfig.getSulConfig() instanceof SulServerConfig);
        SulServerConfig sulServerConfig = (SulServerConfig) stateFuzzerServerConfig.getSulConfig();

        Assert.assertNotNull(learnerConfig);
        Assert.assertEquals(timeLimit, learnerConfig.getTimeLimit());

        Assert.assertNotNull(sulServerConfig);
        Assert.assertEquals(inputResponseTimeoutMap, sulServerConfig.getInputResponseTimeout());
        Assert.assertEquals(connect, sulServerConfig.getHost());
    }

    @Test
    public void parseInvalidCommand() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                "invalidCommand"
        });

        Assert.assertNull(parseResult);
    }

    @Test
    public void parseInvalidOption() {
        CommandLineParser commandLineParser = buildCommandLineParser();
        CommandLineParser.ParseResult parseResult;

        parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_CLIENT,
                "-invalidOption",
                // SulClientConfig required options not asserted here
                "-port", "0",
        });

        Assert.assertNull(parseResult);

        parseResult = commandLineParser.parseCommand(new String[]{
                CommandLineParser.CMD_STATE_FUZZER_SERVER,
                "-invalidOption",
                // SulServerConfig required options not asserted here
                "-connect", "host:1234",
        });

        Assert.assertNull(parseResult);
    }

    @Test
    public void parseMissingRequiredOptions() {
        CommandLineParser commandLineParser = buildCommandLineParser();

        String[] commands = new String[] {CommandLineParser.CMD_STATE_FUZZER_CLIENT, CommandLineParser.CMD_STATE_FUZZER_SERVER};

        for (String command : commands) {
            CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
                    command
                    // omit required options of SulClientConfig and SulServerConfig respectively
            });
            Assert.assertNull(parseResult);
        }
    }

    private StateFuzzerClientConfig assertParseResultOfClient(CommandLineParser.ParseResult parseResult) {
        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_CLIENT, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerClientConfig);
        return (StateFuzzerClientConfig) parseResult.getObjectFromParsedCommand();
    }

    private StateFuzzerServerConfig assertParseResultOfServer(CommandLineParser.ParseResult parseResult) {
        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_SERVER, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerServerConfig);
        return (StateFuzzerServerConfig) parseResult.getObjectFromParsedCommand();
    }

    private CommandLineParser buildCommandLineParser() {
        return new CommandLineParser(new StateFuzzerConfigBuilderImpl(), null, null, null, null);
    }

    private static class StateFuzzerConfigBuilderImpl implements StateFuzzerConfigBuilder {

        @Override
        public StateFuzzerClientConfig buildClientConfig() {
            return new StateFuzzerClientConfig(
                null,
                new SulClientConfigImpl(new MapperConfigStandard(), new SulAdapterConfigStandard()),
                null,
                null
            );
        }

        @Override
        public StateFuzzerServerConfig buildServerConfig() {
            return new StateFuzzerServerConfig(
                null,
                new SulServerConfigImpl(new MapperConfigStandard(), new SulAdapterConfigStandard()),
                null,
                null
            );
        }

        public static class SulServerConfigImpl extends SulServerConfig {

            public SulServerConfigImpl(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
                super(mapperConfig, sulAdapterConfig);
            }

            @Override
            public void applyDelegate(MapperConnectionConfig config) {
            }
        }

        public static class SulClientConfigImpl extends SulClientConfig {

            public SulClientConfigImpl(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
                super(mapperConfig, sulAdapterConfig);
            }

            @Override
            public void applyDelegate(MapperConnectionConfig config) {
            }
        }
    }
}
