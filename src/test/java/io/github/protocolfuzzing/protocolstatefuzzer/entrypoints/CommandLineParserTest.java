package io.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import static io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport.*;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandLineParserTest {
    @Test
    public void parseDynamicOptionsBeforeUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {"-Dpre.fix=test_", "-Dpostfix=_dir", "-output", "${pre.fix}out${postfix}"};

        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseDynamicOptionsAfterUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {"-output", "${pre.fix}out${postfix}", "-Dpre.fix=test_", "-Dpostfix=_dir"};

        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseDynamicOptionsBeforeAndAfterUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {"-Dpre.fix=test_", "-output", "${pre.fix}out${postfix}", "-Dpostfix=_dir"};

        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assertions.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseInvalidCommand() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[] {"invalidCommand"});

        Assertions.assertNull(parseResult);
    }

    @Test
    public void parseInvalidOption() {
        String[] partialArgs = new String[] {"-invalidOption"};

        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        assertInvalidClientParse(commandLineParser, partialArgs);
        assertInvalidServerParse(commandLineParser, partialArgs);
    }

    @Test
    public void parseMissingRequiredOptions() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfigStandard(new SULClientConfigStandard());
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfigStandard(new SULServerConfigStandard());
            }
        });

        // omit required options of SULClientConfigStandard and SULServerConfigStandard
        assertInvalidClientParse(commandLineParser, new String[0]);
        assertInvalidServerParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerClientConfig() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return null;
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfig() {};
            }
        });

        assertInvalidClientParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerServerConfig() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfig() {};
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return null;
            }
        });

        assertInvalidServerParse(commandLineParser, new String[0]);
    }

    @Test
    public void parseDiffTestCommand() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        String[] partialArgs = new String[] {"-model-a", "modelA.dot", "-model-b", "modelB.dot", "-alphabet", "alphabet.xml"};

        DiffTesterConfig diffTesterConfig = parseDiffTestArgs(commandLineParser, partialArgs);
        Assertions.assertEquals("modelA.dot", diffTesterConfig.getModelA());
        Assertions.assertEquals("modelB.dot", diffTesterConfig.getModelB());
    }

    @Test
    public void buildNulldiffTesterconfig() {
        CommandLineParser<?> commandLineParser = new CommandLineParser<>(ConfigBuilders.stateFuzzer(),
            new DiffTesterConfigBuilder() {
                @Override
                public DiffTesterConfig buildConfig() {
                    return null;
                }
            }, ConfigBuilders.fingerprint(), null, null, null, null, null, null);

        assertInvalidDiffTestParse(commandLineParser, new String[0]);
    }

    @Test
    public void parseFingerprintCommand() {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(ConfigBuilders.stateFuzzer());

        String[] partialArgs = new String[] {"-models", "models", "-output", "adg.dot"};
        FingerprintConfig fingerprintConfig = parseFingerprintArgs(commandLineParser, partialArgs);
        Assertions.assertEquals("models", fingerprintConfig.getModelsPath());
        Assertions.assertEquals("adg.dot", fingerprintConfig.getOutputPath());
    }

    @Test
    public void buildNullFingerprintconfig() {
        CommandLineParser<?> commandLineParser = new CommandLineParser<>(ConfigBuilders.stateFuzzer(),
            ConfigBuilders.diffTester(),
            new FingerprintConfigBuilder() {
                @Override
                public FingerprintConfig buildFingerprintConfig() {
                    return null;
                }
            }, null, null, null, null, null, null);

        assertInvalidFingerprintParse(commandLineParser, new String[0]);
    }
}
