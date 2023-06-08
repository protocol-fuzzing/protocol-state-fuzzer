package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Allows to start and stop processes launched by executing a given command.
 * <p>
 * At most one process can be active at a time.
 */
public class ProcessHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the process' command related information. */
    protected ProcessBuilder pb;

    /** Stores the current running process. */
    protected Process currentProcess;

    /** Stores the command used to terminate the process.*/
    protected String terminateCommand;

    /** Stores the stream of the process' normal output. */
    protected OutputStream output;

    /** Stores the stream of the process' error output. */
    protected OutputStream error;

    /** Stores the wait time after the start of the process. */
    protected long startWait;

    /**
     * Indicates if {@link #currentProcess} has been launched successfully
     * at least once, irrespective of whether it has terminated since first execution.
     */
    protected boolean hasLaunched;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param sulConfig  the configuration of the sul
     */
    public ProcessHandler(SulConfig sulConfig) {
        this(sulConfig.getCommand(), sulConfig.getStartWait());

        if (sulConfig.getProcessDir() != null) {
            setDirectory(new File(sulConfig.getProcessDir()));
            LOGGER.info("Directory of SUL process: {}", sulConfig.getProcessDir());
        }

        if (sulConfig.isRedirectOutputStreams()) {
            this.output = System.out;
            this.error = System.err;
        }

        this.terminateCommand = sulConfig.getTerminateCommand();
        if (this.terminateCommand != null) {
            LOGGER.info("Command to terminate SUL process: {}", this.terminateCommand);
        }
    }

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param command    the command of the process
     * @param startWait  the waiting time after the start of the process
     */
    protected ProcessHandler(String command, long startWait) {
        // '+' after \\s takes care of multiple consecutive spaces so that they
        // don't result in empty arguments
        this.pb = new ProcessBuilder(command.split("\\s+"));
        this.startWait = startWait;
        LOGGER.info("Command to launch SUL process: {}", command);
        LOGGER.info("Wait time after launching SUL process: {} ms", startWait);
    }

    /**
     * Sets the value of {@link #output}.
     *
     * @param toOutput  the new output stream
     */
    public void redirectOutput(OutputStream toOutput) {
        output = toOutput;
    }

    /**
     * Sets the value of {@link #error}.
     *
     * @param toOutput  the new error stream
     */
    public void redirectError(OutputStream toOutput) {
        error = toOutput;
    }

    /**
     * Sets the current working directory before the process is launched.
     *
     * @param procDir  the directory of the process
     */
    public void setDirectory(File procDir) {
        pb.directory(procDir);
    }

    /**
     * Launches a process which executes the handler's command, but does nothing
     * if the process has been already launched.
     * <p>
     * Also sets {@link #hasLaunched} to true on successful launch of the process
     * and after launching, sleeps for {@link #startWait} milliseconds.
     */
    public void launchProcess() {
        if (currentProcess != null) {
            LOGGER.warn("Process has already been started");
            return;
        }

        try {
            currentProcess = pb.start();
            hasLaunched = true;

            if (output != null) {
                inheritIO(currentProcess.getInputStream(), new PrintStream(output, true, StandardCharsets.UTF_8));
            }

            if (error != null) {
                inheritIO(currentProcess.getErrorStream(), new PrintStream(error, true, StandardCharsets.UTF_8));
            }

            if (startWait > 0) {
                Thread.sleep(startWait);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Couldn't start process due to exec:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Terminates the process executing the handler's command, but does nothing
     * if the process has been already terminated.
     */
    public void terminateProcess() {
        if (currentProcess == null) {
            LOGGER.warn("Process has already been ended");
            return;
        }

        if (terminateCommand == null) {
            currentProcess.destroyForcibly();
            currentProcess = null;
            return;
        }

        try {
            // '+' after \\s takes care of multiple consecutive spaces
            Runtime.getRuntime().exec(terminateCommand.split("\\s+"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            currentProcess = null;
        }
    }

    /**
     * Returns {@code true} if {@link #currentProcess} is alive.
     *
     * @return  {@code true} if {@link #currentProcess} is alive
     */
    public boolean isAlive() {
        return currentProcess != null && currentProcess.isAlive();
    }

    /**
     * Returns the value of {@link #hasLaunched}.
     *
     * @return  the value of {@link #hasLaunched}
     */
    public boolean hasLaunched() {
        return hasLaunched;
    }

    /**
     * Starts a new thread that copies the source stream to the destination stream.
     *
     * @param src   the source stream
     * @param dest  the destination stream
     */
    protected void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(() -> {
            Scanner sc = new Scanner(src, StandardCharsets.UTF_8);
            while (sc.hasNextLine()) {
                dest.println(sc.nextLine());
            }
            sc.close();
        }).start();
    }
}
