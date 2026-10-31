package io.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

/**
 * Helpers for tests that parse command-line arguments into configs.
 * <p>
 * Lives in the entrypoints package to access the protected parsing API of {@link CommandLineParser}.
 */
public final class CliTestSupport {

    private CliTestSupport() {}

    /**
     * Creates a parser that can only parse configs, since it has no component builders.
     *
     * @param  stateFuzzerConfigBuilder the state fuzzer config builder to use
     *
     * @return                          the command-line parser
     */
    public static CommandLineParser<Object> parser(StateFuzzerConfigBuilder stateFuzzerConfigBuilder) {
        return new CommandLineParser<>(stateFuzzerConfigBuilder, ConfigBuilders.diffTester(),
            ConfigBuilders.fingerprint(), null, null, null, null, null, null);
    }

    public static String[] concatArgs(String[] args1, String[] args2) {
        String[] args = Arrays.copyOf(args1, args1.length + args2.length);
        System.arraycopy(args2, 0, args, args1.length, args2.length);
        return args;
    }

    public static StateFuzzerClientConfig parseClientArgs(CommandLineParser<?> commandLineParser,
        String[] partialArgs) {
        return parseValid(commandLineParser, CommandLineParser.CMD_STATE_FUZZER_CLIENT, partialArgs,
            StateFuzzerClientConfig.class);
    }

    public static StateFuzzerServerConfig parseServerArgs(CommandLineParser<?> commandLineParser,
        String[] partialArgs) {
        return parseValid(commandLineParser, CommandLineParser.CMD_STATE_FUZZER_SERVER, partialArgs,
            StateFuzzerServerConfig.class);
    }

    public static DiffTesterConfig parseDiffTestArgs(CommandLineParser<?> commandLineParser, String[] partialArgs) {
        return parseValid(commandLineParser, CommandLineParser.CMD_DIFF_TEST, partialArgs, DiffTesterConfig.class);
    }

    public static FingerprintConfig parseFingerprintArgs(CommandLineParser<?> commandLineParser,
        String[] partialArgs) {
        return parseValid(commandLineParser, CommandLineParser.CMD_FINGERPRINT, partialArgs,
            FingerprintConfig.class);
    }

    public static void assertInvalidClientParse(CommandLineParser<?> commandLineParser, String[] partialArgs) {
        assertInvalid(commandLineParser, CommandLineParser.CMD_STATE_FUZZER_CLIENT, partialArgs);
    }

    public static void assertInvalidServerParse(CommandLineParser<?> commandLineParser, String[] partialArgs) {
        assertInvalid(commandLineParser, CommandLineParser.CMD_STATE_FUZZER_SERVER, partialArgs);
    }

    public static void assertInvalidDiffTestParse(CommandLineParser<?> commandLineParser, String[] partialArgs) {
        assertInvalid(commandLineParser, CommandLineParser.CMD_DIFF_TEST, partialArgs);
    }

    public static void assertInvalidFingerprintParse(CommandLineParser<?> commandLineParser, String[] partialArgs) {
        assertInvalid(commandLineParser, CommandLineParser.CMD_FINGERPRINT, partialArgs);
    }

    private static <C> C parseValid(CommandLineParser<?> commandLineParser, String command, String[] partialArgs,
        Class<C> configClass) {
        String[] args = concatArgs(new String[] {command}, partialArgs);
        CommandLineParser.ParseResult parseResult = commandLineParser.parseCommand(args);

        Assertions.assertNotNull(parseResult);
        Assertions.assertTrue(parseResult.isValid());
        Assertions.assertEquals(command, parseResult.getCommander().getParsedCommand());
        return Assertions.assertInstanceOf(configClass, parseResult.getObjectFromParsedCommand());
    }

    private static void assertInvalid(CommandLineParser<?> commandLineParser, String command, String[] partialArgs) {
        String[] args = concatArgs(new String[] {command}, partialArgs);
        Assertions.assertNull(commandLineParser.parseCommand(args));
    }
}
