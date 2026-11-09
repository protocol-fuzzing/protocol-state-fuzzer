package io.github.protocolfuzzing.protocolstatefuzzer.e2e;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs scenarios through the command line, as a real fuzzer is used.
 * Each command is an args file, passed as the first argument,
 * followed by {@code -D} definitions of the properties it uses.
 * <p>
 * The commands running a scenario's endpoint are in the scenario's
 * {@code args/} directory, and use {@value #SCENARIOS_DIR} and
 * {@value #WORK_DIR} (see {@link ScenarioRun#command(String)}).
 */
public final class ScenarioCli {

    /** The directory of all scenarios of the kind, e.g. of the Mealy scenarios. */
    public static final String SCENARIOS_DIR = "scenarios.dir";

    /** The work directory of a run, prepared by a {@link ScenarioRun}. */
    public static final String WORK_DIR = "work.dir";

    public static final String LEARN = "args/learn";
    public static final String TEST = "args/test";

    private ScenarioCli() {}

    /**
     * Builds a command running an args file.
     *
     * @param  argsFile   the args file
     * @param  properties the properties to define
     *
     * @return            the command-line arguments
     */
    public static String[] command(Path argsFile, Map<String, String> properties) {
        List<String> args = new ArrayList<>(List.of(argsFile.toString()));
        properties.forEach((name, value) -> args.add("-D" + name + "=" + value));
        return args.toArray(String[]::new);
    }

    /**
     * Chains commands into one command-line call, separated by {@code --}.
     *
     * @param  commands the commands
     *
     * @return          the command-line arguments
     */
    public static String[] chain(String[]... commands) {
        List<String> args = new ArrayList<>();
        for (String[] command: commands) {
            if (!args.isEmpty()) {
                args.add("--");
            }
            args.addAll(List.of(command));
        }
        return args.toArray(String[]::new);
    }

    /**
     * Returns the value of an option in an args file, read as the command line
     * reads it, with one argument per line and lines starting with {@code #}
     * ignored.
     *
     * @param  argsFile the args file
     * @param  option   the option, e.g. {@code -processDir}
     *
     * @return          the value following the option
     */
    public static String optionValue(Path argsFile, String option) {
        List<String> args;
        try {
            args = Files.readAllLines(argsFile, StandardCharsets.UTF_8).stream()
                .map(String::trim).filter(line -> !line.isEmpty() && !line.startsWith("#")).toList();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        int index = args.indexOf(option);
        Assertions.assertTrue(index >= 0 && index + 1 < args.size(), () -> "no " + option + " in " + argsFile);
        return args.get(index + 1);
    }

    /**
     * Checks that each args file the scenario has selects the scenario's own
     * endpoint with {@code -processDir}. This catches files copied from
     * another scenario and not updated.
     *
     * @param scenario  the scenario
     * @param argsFiles the args files, relative to the scenario directory
     */
    public static void assertArgsFilesSelectScenario(Scenario scenario, List<String> argsFiles) {
        String processDir = "${" + SCENARIOS_DIR + "}/" + scenario.getId();
        for (String argsFile: argsFiles) {
            scenario.findFile(argsFile).ifPresent(file -> Assertions.assertEquals(processDir,
                optionValue(file, "-processDir"), () -> "-processDir of " + argsFile));
        }
    }

    /**
     * Checks that the output directory holds the command's arguments, that is the args
     * file's content, then the other arguments.
     *
     * @param command   the command, whose first argument is its args file
     * @param outputDir the command's output directory
     */
    public static void assertArgsKept(List<String> command, Path outputDir) {
        try {
            List<String> expected = new ArrayList<>(Files.readAllLines(Path.of(command.get(0)),
                StandardCharsets.UTF_8));
            expected.addAll(command.subList(1, command.size()));
            Assertions.assertEquals(expected,
                Files.readAllLines(outputDir.resolve(CliTestSupport.ARGS_FILE), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
