package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.*;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.DotProcessor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the provided command-line arguments and initiates the appropriate
 * action; starts the fuzzing or the testing.
 */
public class CommandLineParser {
    private static final Logger LOGGER = LogManager.getLogger();

    /** JCommander command name for fuzzing client implementations. */
    protected static final String CMD_STATE_FUZZER_CLIENT = "state-fuzzer-client";

    /** JCommander command name for fuzzing server implementations. */
    protected static final String CMD_STATE_FUZZER_SERVER = "state-fuzzer-server";

    /** Name of the file in which the arguments will be saved. */
    protected static final String ARGS_FILE = "command.args";

    /** Stores the program name that appears in usage; defaults to {@code "<mainClass>"}. */
    protected String programName = "<mainClass>";

    /** Stores the constructor parameter. */
    protected StateFuzzerConfigBuilder stateFuzzerConfigBuilder;

    /** Stores the constructor parameter. */
    protected StateFuzzerBuilder stateFuzzerBuilder;

    /** Stores the constructor parameter. */
    protected TestRunnerBuilder testRunnerBuilder;

    /** Stores the constructor parameter. */
    protected TimingProbeBuilder timingProbeBuilder;

    /** List of external Logger names that can have their logging level changed. */
    protected String[] externalParentLoggers;

    /**
     * Extracts from the given packageName at most the first depth components.
     * <ul>
     * <li> If packageName = "suffix.inner2.inner1.base.name" and depth = 4
     *      then basePackageName = "suffix.inner2.inner1.base".
     * <li> If {@code depth < 1 or depth > (depth of packageName)}
     *      then basePackageName = packageName.
     * </ul>
     *
     * @param packageName  the name of the package from which the base name will be derived
     * @param depth        the depth of the original package name to keep in the derived one
     * @return             the derived base package name; if it cannot be derived,
     *                     then the provided package name is returned
     */
    public static String getBasePackageName(String packageName, int depth){
        if (depth < 1) {
            return packageName;
        }

        // pattern matches {a}.{a}.{a}.{a} depth times, where a is anything other than '.'
        // at first {a} (anything other than '.') and then (depth - 1) times '.{a}'
        String pattern = String.format("[^\\.]*(\\.[^\\.]*){%s}", depth - 1);
        Matcher matcher = Pattern.compile(pattern).matcher(packageName);
        return matcher.find() ? matcher.group() : packageName;
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param stateFuzzerConfigBuilder  the builder of the StateFuzzerClientConfig
     *                                  and StateFuzzerServerConfig
     * @param stateFuzzerBuilder        the builder of the StateFuzzer
     * @param testRunnerBuilder         the builder of the TestRunner
     * @param timingProbeBuilder        the builder of the TimingProbe
     */
    public CommandLineParser(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, StateFuzzerBuilder stateFuzzerBuilder,
                             TestRunnerBuilder testRunnerBuilder, TimingProbeBuilder timingProbeBuilder){
        this.stateFuzzerBuilder = stateFuzzerBuilder;
        this.stateFuzzerConfigBuilder = stateFuzzerConfigBuilder;
        this.testRunnerBuilder = testRunnerBuilder;
        this.timingProbeBuilder =  timingProbeBuilder;
    }

    /**
     * Sets the program name that appears in usage; to be used before parsing.
     *
     * @param programName  the name of the program
     */
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    /**
     * Sets the external parent logger names, whose logging level will also be updated
     * after parsing the corresponding JCommander Parameters of {@link StateFuzzerConfigStandard}.
     * <p>
     * In order to take effect, this function should be called before parsing.
     *
     * @param externalParentLoggers  the external parent logger names
     */
    public void setExternalParentLoggers(String[] externalParentLoggers) {
        this.externalParentLoggers = externalParentLoggers;
    }

    /**
     * Parses and executes the arguments, optionally converts the learned DOT
     * models to PDF and uses the provided consumers consecutively on the results.
     * <p>
     * Multiple independent commands can be separated using {@literal --}.
     *
     * @param args         the command-line arguments to be parsed
     * @param exportToPDF  {@code true} if the DOT models should be exported to PDF
     * @param consumers    the list of consumers to be used consecutively on the results
     * @return             the list of each command's learning result
     */
    public List<LearnerResult> parse(String[] args, boolean exportToPDF, List<Consumer<LearnerResult>> consumers) {
        int startCmd;
        int endCmd = 0;
        String[] cmdArgs;

        if (args.length == 0) {
            // to show global usage
            parseAndExecuteCommand(args);
        }

        List<LearnerResult> results = new ArrayList<>();
        while (args.length > endCmd) {
            startCmd = endCmd;
            while (args.length > endCmd && !args[endCmd].equals("--")) {
                endCmd++;
            }
            cmdArgs = Arrays.copyOfRange(args, startCmd, endCmd);

            // parse and execute
            LearnerResult result = parseAndExecuteCommand(cmdArgs);

            // post process
            if (exportToPDF) {
                DotProcessor.exportToPDF(result);
            }

            for (Consumer<LearnerResult> con: consumers) {
                if (con != null) {
                    con.accept(result);
                }
            }

            results.add(result);
            endCmd++;
        }

        return results;
    }

    /**
     * Parses and executes the arguments and optionally converts the learned
     * DOT models to PDF.
     * <p>
     * Multiple independent commands can be separated using {@literal --}.
     *
     * @param args         the command-line arguments to be parsed
     * @param exportToPDF  {@code true} if the DOT models should be exported to PDF
     * @return             the list of each command's learning result
     */
    public List<LearnerResult> parse(String[] args, boolean exportToPDF) {
        return parse(args, exportToPDF, List.of());
    }


    /**
     * Parses and executes the arguments and returns the results.
     * <p>
     * Multiple independent commands can be separated using {@literal --}.
     *
     * @param args  the command-line arguments to be parsed
     * @return      the list of each command's learning result
     */
    public List<LearnerResult> parse(String[] args) {
        return parse(args, false, List.of());
    }

    /**
     * Parses the arguments provided and executes the specified command.
     * <p>
     * It uses the {@link #parseCommand(String[])} and {@link #executeCommand(ParseResult)}.
     *
     * @param args  the command-line arguments to be parsed
     * @return      if the command involves state fuzzing then the corresponding LearnerResult,
     *              which can be empty if fuzzing fails, otherwise an empty LearnerResult
     */
    protected LearnerResult parseAndExecuteCommand(String[] args) {
        try {
            return executeCommand(parseCommand(args));
        } catch (Exception e) {
            LOGGER.error("Encountered an exception, see below for more info");
            e.printStackTrace();
            return new LearnerResult().toEmpty();
        }
    }

    /**
     * Parses the arguments provided and returns a ParseResult.
     * <p>
     * It uses a dual parse technique, in order to properly parse the dynamically
     * defined properties wherever they are. The first parse stores only the
     * dynamically defined properties and then the second parse reparses all
     * the arguments and effectively resolves all of the property placeholders.
     * <p>
     * It uses the {@link #buildCommander(boolean, StateFuzzerClientConfig, StateFuzzerServerConfig)}
     * for the acquisition of different JCommanders per parse.
     *
     * @param args  the command-line arguments to be parsed
     * @return      the ParseResult that contains the arguments and the
     *              JCommander instance used to parse them
     */
    protected ParseResult parseCommand(String[] args) {
        PropertyResolver.initializeParsing();

        StateFuzzerClientConfig stateFuzzerClientConfig = stateFuzzerConfigBuilder.buildClientConfig();
        StateFuzzerServerConfig stateFuzzerServerConfig = stateFuzzerConfigBuilder.buildServerConfig();

        JCommander commander = buildCommander(true, stateFuzzerClientConfig, stateFuzzerServerConfig);

        if (args.length > 0
                && !commander.getCommands().containsKey(args[0])
                && !args[0].startsWith("@")
                && new File(args[0]).exists()) {
            LOGGER.info("The first argument is a file path. Processing it as an argument file.");
            args[0] = "@" + args[0];
        }

        try {
            // parse only dynamic parameters on first parse
            commander.parse(args);

            // first parse succeeded, so parse the arguments normally
            commander = buildCommander(false, stateFuzzerClientConfig, stateFuzzerServerConfig);
            commander.parse(args);

            return new ParseResult(args, commander);

        } catch (ParameterException e) {
            LOGGER.error("Parameter parse error: {}", e.getMessage());
            return null;

        } finally {
            PropertyResolver.finalizeParsing();
        }
    }

    /**
     * Retrieves and executes the parsed command from the JCommander instance
     * contained in the ParseResult parameter.
     * <p>
     * The possible executions are: 1) testing using a test runner or a
     * timing probe or 2) fuzzing using the state fuzzer.
     *
     * @param parseResult  the ParseResult with the parsed arguments and the
     *                     JCommander instance used
     * @return             if the command involves state fuzzing then the
     *                     corresponding LearnerResult, which can be empty if
     *                     fuzzing fails, otherwise an empty LearnerResult
     */
    protected LearnerResult executeCommand(ParseResult parseResult) {
        LearnerResult emptyLearnerResult = new LearnerResult().toEmpty();

        if (parseResult == null || !parseResult.isValid()) {
            return emptyLearnerResult;
        }

        String parsedCommand = parseResult.getCommander().getParsedCommand();
        if (parsedCommand == null) {
            parseResult.getCommander().usage();
            return emptyLearnerResult;
        }

        StateFuzzerConfig stateFuzzerConfig = (StateFuzzerConfig) parseResult.getObjectFromParsedCommand();
        if (stateFuzzerConfig == null || stateFuzzerConfig.isHelp()) {
            parseResult.getCommander().usage();
            return emptyLearnerResult;
        }

        LOGGER.info("Processing command {}", parsedCommand);

        if (stateFuzzerConfig.isDebug()) {
            updateLoggingLevel(externalParentLoggers, Level.DEBUG);
        } else if (stateFuzzerConfig.isQuiet()) {
            updateLoggingLevel(externalParentLoggers, Level.ERROR);
        }

        // check if test options have been supplied for launching the available test runners
        if (stateFuzzerConfig.getTestRunnerConfig().getTest() != null) {
            LOGGER.info("Test option is found");

            if (stateFuzzerConfig.getTimingProbeConfig().getProbeCmd() != null) {
                LOGGER.info("Running timing probe");
                timingProbeBuilder.build(stateFuzzerConfig).run();
            } else {
                LOGGER.info("Running test runner");
                testRunnerBuilder.build(stateFuzzerConfig).run();
            }

            return emptyLearnerResult;
        }

        // run state fuzzer
        LOGGER.info("State-fuzzing a {} implementation", stateFuzzerConfig.getSulConfig().getFuzzingRole());

        // this is an extra step done to store the running arguments
        prepareOutputDir(parseResult.getArgs(), stateFuzzerConfig.getOutputDir());

        return stateFuzzerBuilder.build(stateFuzzerConfig).startFuzzing();
    }

    /**
     * Builds a JCommander instance for the dual parsing technique.
     * <p>
     * The first boolean parameter determines the type of configuration that will
     * be used; either the one that stores only dynamic parameters (first parse)
     * or the one that parses all the parameters (second parse).
     *
     * @param parseOnlyDynamicParameters  boolean that determines the type of
     *                                    the JCommander instance to be built
     * @param stateFuzzerClientConfig     the configuration of the client fuzzing command
     *                                    used in the second parse
     * @param stateFuzzerServerConfig     the configuration of the server fuzzing command
     *                                    used in the second parse
     * @return                            a new instance of the specified JCommander
     */
    protected JCommander buildCommander(boolean parseOnlyDynamicParameters,
        StateFuzzerClientConfig stateFuzzerClientConfig,
        StateFuzzerServerConfig stateFuzzerServerConfig) {

        if (parseOnlyDynamicParameters) {
            // having only PropertyResolver as Object to commands
            // only dynamic parameters are parsed and stored without any converter

            return JCommander.newBuilder()
                    .allowParameterOverwriting(true)
                    .programName(programName)
                    .addCommand(CMD_STATE_FUZZER_CLIENT, stateFuzzerClientConfig.getPropertyResolver())
                    .addCommand(CMD_STATE_FUZZER_SERVER, stateFuzzerServerConfig.getPropertyResolver())
                    .acceptUnknownOptions(true)
                    .build();
        }

        // normal parse with all converters active
        return JCommander.newBuilder()
                .allowParameterOverwriting(true)
                .programName(programName)
                .addCommand(CMD_STATE_FUZZER_CLIENT, stateFuzzerClientConfig)
                .addCommand(CMD_STATE_FUZZER_SERVER, stateFuzzerServerConfig)
                .addConverterFactory(new BasicConverterFactory())
                .build();
    }

    /**
     * Updates the logging level of ProtocolState-Fuzzer and of the external
     * parent loggers to the specified level.
     * <p>
     * If the provided array for the external parent loggers is null or empty,
     * then only the logging level of ProtocolState-Fuzzer is updated.
     *
     * @param externalParentLoggers  list of Logger names external to this project
     * @param level                  the logging level to be set
     */
    protected void updateLoggingLevel(String[] externalParentLoggers, Level level) {
        String ownParentLogger = getBasePackageName(this.getClass().getPackageName(), 4);
        Configurator.setAllLevels(ownParentLogger, level);

        if (externalParentLoggers != null) {
            for (String externalParentLogger: externalParentLoggers) {
                Configurator.setAllLevels(externalParentLogger, level);
            }
        }
    }

    /**
     * Creates the output directory for this parse and copies there the
     * command-line arguments used.
     * <p>
     * For the copying of the arguments it uses the {@link #copyArgsToOutDir(String[], String)}.
     *
     * @param args    the arguments that were parsed
     * @param outDir  the output directory name of this parse
     */
    protected void prepareOutputDir(String[] args, String outDir) {
        File dirFile = new File(outDir);
        if (!dirFile.exists()) {
            boolean ok = dirFile.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create output directory: " + outDir);
            }
        }

        try {
            copyArgsToOutDir(args, outDir);
        } catch (IOException e) {
            LOGGER.error("Failed to copy arguments");
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    /**
     * Copies the arguments used in this parse to the {@link #ARGS_FILE} in the outDir.
     * <p>
     * All the arguments provided either in a file or in the command-line are copied.
     *
     * @param args    the arguments that were parsed
     * @param outDir  the output directory name of this parse
     *
     * @throws IOException  if an error during writing occurs
     */
    protected void copyArgsToOutDir(String[] args, String outDir) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(outDir, ARGS_FILE))) {
            for (String arg : args) {
                if (!arg.startsWith("@")) {
                    // command-line argument
                    fileOutputStream.write((arg + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                } else {
                    // file containing arguments
                    File argsFile = new File(arg.substring(1));

                    if (!argsFile.exists()) {
                        LOGGER.warn("Arguments file " + argsFile.getPath() + " has been moved");
                        continue;
                    }

                    try (FileInputStream fis = new FileInputStream(argsFile)) {
                        int bytesRead;
                        byte[] byteArray = new byte[1024];
                        while ((bytesRead = fis.read(byteArray, 0, byteArray.length)) > 0) {
                            fileOutputStream.write(byteArray, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }

    /**
     * Used to store the arguments that were parsed and the JCommander instance
     * used for parsing.
     */
    protected static class ParseResult {

        /** The arguments that were used. */
        protected String[] args;

        /** The JCommander instance used for parsing the arguments. */
        protected JCommander commander;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param args       the arguments that were parsed
         * @param commander  the JCommander instance that parsed the arguments
         */
        public ParseResult(String[] args, JCommander commander) {
            this.args = args;
            this.commander = commander;
        }

        /**
         * Returns the arguments.
         *
         * @return  the arguments
         */
        public String[] getArgs() {
            return args;
        }

        /**
         * Returns the JCommander instance.
         *
         * @return  the JCommander instance
         */
        public JCommander getCommander() {
            return commander;
        }

        /**
         * Checks if the ParseResult is valid.
         *
         * @return  {@code true} if neither of {@link #args} or
         *          {@link #commander} are null
         */
        public boolean isValid() {
            return args != null && commander != null;
        }

        /**
         * Gets the object at index 0 using {@link #getObjectFromParsedCommand(int)}
         *
         * @return  the object at index 0
         */
        public Object getObjectFromParsedCommand() {
            return getObjectFromParsedCommand(0);
        }

        /**
         * Gets the object at the specified index of the parsed JCommander command.
         * <p>
         * Objects are all the associated objects with a JCommander command, like
         * a StateFuzzerClientConfig or StateFuzzerServerConfig used in
         * {@link CommandLineParser}. The downcasting is left to the user.
         *
         * @param index  the index of the JCommander objects
         * @return       the object at this index
         */
        public Object getObjectFromParsedCommand(int index) {
            if (commander == null
                    || commander.getCommands() == null || commander.getCommands().isEmpty()
                    || commander.getParsedCommand() == null) {
                return null;
            }

            JCommander parsedCommander = commander.getCommands().get(commander.getParsedCommand());
            if (parsedCommander == null) {
                return null;
            }

            try {
                return parsedCommander.getObjects().get(index);
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("get Object from parsed command: " + e.getMessage());
                return null;
            }
        }
    }
}
