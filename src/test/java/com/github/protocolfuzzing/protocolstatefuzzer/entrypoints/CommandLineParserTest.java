package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class CommandLineParserTest<M> {
    @Test
    public void parseDynamicOptionsBeforeUsage() {
        String output = "test_out_dir";

        String[] partialArgs = new String[] {
            "-Dpre.fix=test_",
            "-Dpostfix=_dir",
            "-output", "${pre.fix}out${postfix}"
        };

        CommandLineParser<M> commandLineParser = new CommandLineParser<>(new StateFuzzerConfigBuilderSimple(), null, null, null);

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

        CommandLineParser<M> commandLineParser = new CommandLineParser<>(new StateFuzzerConfigBuilderSimple(), null, null, null);

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

        CommandLineParser<M> commandLineParser = new CommandLineParser<>(new StateFuzzerConfigBuilderSimple(), null, null, null);

        // parse as client command
        StateFuzzerClientConfig stateFuzzerClientConfig = parseClientArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerClientConfig.getOutputDir());

        // parse as server command
        StateFuzzerServerConfig stateFuzzerServerConfig = parseServerArgs(commandLineParser, partialArgs);
        Assert.assertEquals(output, stateFuzzerServerConfig.getOutputDir());
    }

    @Test
    public void parseInvalidCommand() {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(new StateFuzzerConfigBuilderSimple(), null, null, null);

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

        CommandLineParser<M> commandLineParser = new CommandLineParser<>(new StateFuzzerConfigBuilderSimple(), null, null, null);

        assertInvalidClientParse(commandLineParser, partialArgs);
        assertInvalidServerParse(commandLineParser, partialArgs);
    }

    @Test
    public void parseMissingRequiredOptions() {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfigStandard(new SulClientConfigStandard());
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfigStandard(new SulServerConfigStandard());
                }
            }, null, null, null);

        // omit required options of SulClientConfigStandard and SulServerConfigStandard
        assertInvalidClientParse(commandLineParser, new String[0]);
        assertInvalidServerParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerClientConfig() {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return null;
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return new StateFuzzerServerConfig(){};
                }
            }, null, null, null);

        assertInvalidClientParse(commandLineParser, new String[0]);
    }

    @Test
    public void buildNullStateFuzzerServerConfig() {
        CommandLineParser<M> commandLineParser = new CommandLineParser<>(
            new StateFuzzerConfigBuilder() {
                @Override
                public StateFuzzerClientConfig buildClientConfig() {
                    return new StateFuzzerClientConfig(){};
                }
                @Override
                public StateFuzzerServerConfig buildServerConfig() {
                    return null;
                }
            }, null, null, null);

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

    public static <M> StateFuzzerClientConfig parseClientArgs(CommandLineParser<M> commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_CLIENT}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);

        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_CLIENT, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerClientConfig);

        return (StateFuzzerClientConfig) parseResult.getObjectFromParsedCommand();
    }

    public static <M> StateFuzzerServerConfig parseServerArgs(CommandLineParser<M> commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_SERVER}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);

        Assert.assertNotNull(parseResult);
        Assert.assertTrue(parseResult.isValid());
        Assert.assertEquals(CommandLineParser.CMD_STATE_FUZZER_SERVER, parseResult.getCommander().getParsedCommand());
        Assert.assertTrue(parseResult.getObjectFromParsedCommand() instanceof StateFuzzerServerConfig);

        return (StateFuzzerServerConfig) parseResult.getObjectFromParsedCommand();
    }

    public static <M> void assertInvalidClientParse(CommandLineParser<M> commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_CLIENT}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);
        Assert.assertNull(parseResult);
    }

    public static <M> void assertInvalidServerParse(CommandLineParser<M> commandLineParser, String[] partialArgs) {
        String[] args = concatArgs(new String[] {CommandLineParser.CMD_STATE_FUZZER_SERVER}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);
        Assert.assertNull(parseResult);
    }
}
