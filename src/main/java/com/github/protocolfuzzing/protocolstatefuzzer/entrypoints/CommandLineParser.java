package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.*;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineParser {
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final String CMD_STATE_FUZZER_CLIENT = "state-fuzzer-client";
    protected static final String CMD_STATE_FUZZER_SERVER = "state-fuzzer-server";
    protected static final String ARGS_FILE = "command.args";

    protected StateFuzzerBuilder stateFuzzerBuilder;
    protected StateFuzzerConfigBuilder stateFuzzerConfigBuilder;
    protected TestRunnerBuilder testRunnerBuilder;
    protected TimingProbeBuilder timingProbeBuilder;

    protected String[] externalParentLoggers;

    /*
     * Extracts from packageName the basePackageName containing, at most, the first depth components
     * For packageName = "suffix.inner2.inner1.base.name" and depth = 4
     *   then basePackageName = "suffix.inner2.inner1.base"
     * For depth < 1 or depth > (depth of packageName)
     *   then basePackageName = packageName
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

    public CommandLineParser(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, StateFuzzerBuilder stateFuzzerBuilder,
                             TestRunnerBuilder testRunnerBuilder, TimingProbeBuilder timingProbeBuilder,
                             String[] externalParentLoggers){
        Configurator.setLevel(LOGGER, Level.INFO);
        this.stateFuzzerBuilder = stateFuzzerBuilder;
        this.stateFuzzerConfigBuilder = stateFuzzerConfigBuilder;
        this.testRunnerBuilder = testRunnerBuilder;
        this.timingProbeBuilder =  timingProbeBuilder;
        this.externalParentLoggers = externalParentLoggers;
    }

    public void parse(String[] args){
        int startCmd;
        int endCmd = 0;
        String[] cmdArgs;

        if (args.length == 0) {
            // to show global usage
            parseAndExecuteCommand(args);
        }

        while (args.length > endCmd) {
            startCmd = endCmd;
            while (args.length > endCmd && !args[endCmd].equals("--")) {
                endCmd++;
            }
            cmdArgs = Arrays.copyOfRange(args, startCmd, endCmd);
            parseAndExecuteCommand(cmdArgs);
            endCmd++;
        }
    }

    protected void parseAndExecuteCommand(String[] args) {
        try {
            executeCommand(parseCommand(args));
        } catch (Exception e) {
            LOGGER.error("Encountered an exception, see below for more info");
            e.printStackTrace();
        }
    }

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

    protected void executeCommand(ParseResult parseResult) {

        if (parseResult == null || !parseResult.isValid()) {
            return;
        }

        String parsedCommand = parseResult.getCommander().getParsedCommand();
        if (parsedCommand == null) {
            parseResult.getCommander().usage();
            return;
        }

        StateFuzzerConfig stateFuzzerConfig = (StateFuzzerConfig) parseResult.getObjectFromParsedCommand();
        if (stateFuzzerConfig == null || stateFuzzerConfig.isHelp()) {
            parseResult.getCommander().usage();
            return;
        }

        LOGGER.info("Processing command {}", parsedCommand);

        String ownParentLogger = getBasePackageName(this.getClass().getPackageName(), 4);
        if (stateFuzzerConfig.isDebug()) {
            updateLoggingLevels(ownParentLogger, externalParentLoggers, Level.DEBUG);
        } else if (stateFuzzerConfig.isQuiet()) {
            updateLoggingLevels(ownParentLogger, externalParentLoggers, Level.ERROR);
        } else {
            updateLoggingLevels(ownParentLogger, externalParentLoggers, Level.INFO);
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
        } else {
            // run state fuzzer
            LOGGER.info("State-fuzzing a {} implementation", stateFuzzerConfig.getSulConfig().getFuzzingRole());

            // this is an extra step done to store the running arguments
            prepareOutputDir(parseResult.getArgs(), stateFuzzerConfig.getOutputDir());

            stateFuzzerBuilder.build(stateFuzzerConfig).startFuzzing();
        }
    }

    protected JCommander buildCommander(boolean parseOnlyDynamicParameters,
        StateFuzzerClientConfig stateFuzzerClientConfig,
        StateFuzzerServerConfig stateFuzzerServerConfig) {

        if (parseOnlyDynamicParameters) {
            // having only PropertyResolver as Object to commands
            // only dynamic parameters are parsed and stored without any converter

            return JCommander.newBuilder()
                    .allowParameterOverwriting(true)
                    .programName("")
                    .addCommand(CMD_STATE_FUZZER_CLIENT, stateFuzzerClientConfig.getPropertyResolver())
                    .addCommand(CMD_STATE_FUZZER_SERVER, stateFuzzerServerConfig.getPropertyResolver())
                    .acceptUnknownOptions(true)
                    .build();
        }

        // normal parse with all converters active
        return JCommander.newBuilder()
                .allowParameterOverwriting(true)
                .programName("")
                .addCommand(CMD_STATE_FUZZER_CLIENT, stateFuzzerClientConfig)
                .addCommand(CMD_STATE_FUZZER_SERVER, stateFuzzerServerConfig)
                .addConverterFactory(new BasicConverterFactory())
                .build();
    }

    protected void updateLoggingLevels(String ownParentLogger, String[] externalParentLoggers, Level level) {
        Configurator.setAllLevels(ownParentLogger, level);
        for (String externalParentLogger: externalParentLoggers) {
            Configurator.setAllLevels(externalParentLogger, level);
        }
    }

    /*
     * Creates the output directory in advance in order to store in it the arguments file before the tool is executed.
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

    /*
     * Generates a file comprising the entire command given to fuzzer.
     */
    protected void copyArgsToOutDir(String[] args, String outDir) throws IOException {
        Path outputCommandArgsPath = Path.of(outDir, ARGS_FILE);
        FileOutputStream fw = new FileOutputStream(outputCommandArgsPath.toString());
        PrintStream ps = new PrintStream(fw);

        for (String arg : args) {
            if (arg.startsWith("@")) {
                String argsFileName = arg.substring(1);
                File argsFile = new File(argsFileName);

                if (!argsFile.exists()) {
                    LOGGER.warn("Arguments file " + argsFile + " has been moved");
                } else {
                    Files.copy(argsFile.toPath(), outputCommandArgsPath, StandardCopyOption.REPLACE_EXISTING);
                }

            } else {
                ps.println(arg);
            }
        }
        ps.close();
        fw.close();
    }

    protected static class ParseResult {
        protected String[] args;
        protected JCommander commander;

        public ParseResult(String[] args, JCommander commander) {
            this.args = args;
            this.commander = commander;
        }

        public String[] getArgs() {
            return args;
        }

        public JCommander getCommander() {
            return commander;
        }

        public boolean isValid() {
            return args != null && commander != null;
        }

        public Object getObjectFromParsedCommand() {
            return getObjectFromParsedCommand(0);
        }

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

            return parsedCommander.getObjects().get(index);
        }
    }
}