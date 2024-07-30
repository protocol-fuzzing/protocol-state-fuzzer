package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigEmpty;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class CommandLineParserTest {
    @Test
    public void parseDynamicOptionsBeforeUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {
            "-Dpre.fix=test_",
            "-Dpostfix=_dir",
            "-output", "${pre.fix}out${postfix}"
        };

        CommandLineParser commandLineParser = new CommandLineParser(new StateFuzzerConfigBuilderSimple(), null, null, null, null);

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseDynamicOptionsAfterUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {
            "-output", "${pre.fix}out${postfix}",
            "-Dpre.fix=test_",
            "-Dpostfix=_dir"
        };

        CommandLineParser commandLineParser = new CommandLineParser(new StateFuzzerConfigBuilderSimple(), null, null, null, null);

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseDynamicOptionsBeforeAndAfterUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {
            "-Dpre.fix=test_",
            "-output", "${pre.fix}out${postfix}",
            "-Dpostfix=_dir"
        };

        CommandLineParser commandLineParser = new CommandLineParser(new StateFuzzerConfigBuilderSimple(), null, null, null, null);

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseInvalidCommand() {
        CommandLineParser commandLineParser = new CommandLineParser(new StateFuzzerConfigBuilderSimple(), null, null, null, null);

        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(new String[]{
            "invalidCommand"
        });

        Assert.assertNull(parseResult);
    }

    @Test
    public void parseInvalidOption() {
        String[] partialArgs = new String[] {
            "-invalidOption"
        };

        CommandLineParser commandLineParser = new CommandLineParser(new StateFuzzerConfigBuilderSimple(), null, null, null, null);

        assertInvalidClientParse(commandLineParser, partialArgs);
        assertInvalidServerParse(commandLineParser, partialArgs);
    }

    @Test
    public void parseMissingRequiredOptions() {
        CommandLineParser commandLineParser = new CommandLineParser(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null, new SulClientConfigStandard(null, null), null, null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null, new SulServerConfigStandard(null, null), null, null);
                }
            }, null, null, null, null);

        // omit required options of SulClientConfigStandard and SulServerConfigStandard
        assertInvalidClientParse(commandLineParser, new String[0]);
        assertInvalidServerParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerClientConfig() {
        CommandLineParser commandLineParser = new CommandLineParser(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return null;
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigEmpty(null);
                }
            }, null, null, null, null);

        assertInvalidClientParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerServerConfig() {
        CommandLineParser commandLineParser = new CommandLineParser(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigEmpty(null);
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return null;
                }
            }, null, null, null, null);

        assertInvalidServerParse(commandLineParser, new String[0]);
    }

    private static class StateFuzzerConfigBuilderSimple implements StateFuzzerConfigBuilder {
        @Override
        public StateFuzzerClientConfig buildClientConfig() {
            return new StateFuzzerClientConfigStandard(null);
        }
        @Override
        public StateFuzzerServerConfig buildServerConfig() {
            return new StateFuzzerServerConfigStandard(null);
        }
    }

    /* Static methods available to other test files too */

    public static String[] concatArgs(String[] args1, String[] args2) {
        String[] args = Arrays.copyOf(args1, args1.length + args2.length);
        System.arraycopy(args2, 0, args, args1.length, args2.length);
        return args;
    }

    public static StateFuzzerClientConfig parseClientArgs(CommandLineParser commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_CLIENT}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);

        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_CLIENT, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerClientConfig);

        return (StateFuzzerClientConfig) parseResult.getObjectFromParsedCommand();
    }

    public static StateFuzzerServerConfig parseServerArgs(CommandLineParser commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_SERVER}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);

        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_SERVER, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerServerConfig);

        return (StateFuzzerServerConfig) parseResult.getObjectFromParsedCommand();
    }

    public static void assertInvalidClientParse(CommandLineParser commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_CLIENT}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);
        Assert.assertNull(parseResult);
    }

    public static void assertInvalidServerParse(CommandLineParser commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_SERVER}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);
        Assert.assertNull(parseResult);
    }
}
