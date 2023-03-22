package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Scanner;

/**
 * Allows one to start/stop processes launched by executing a given command. At
 * most one process can be executing at a time.
 *
 * @author Paul
 */
public class ProcessHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final ProcessBuilder pb;
    protected Process currentProcess;
    protected String terminateCommand;
    protected OutputStream output;
    protected OutputStream error;
    protected long startWait;
    protected boolean hasLaunched;

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

    protected ProcessHandler(String command, long startWait) {
        // '+' after \\s takes care of multiple consecutive spaces so that they
        // don't result in empty arguments
        this.pb = new ProcessBuilder(command.split("\\s+"));
        this.startWait = startWait;
        LOGGER.info("Command to launch SUL process: {}", command);
        LOGGER.info("Wait time after launching SUL process: {} ms", startWait);
    }

    public void redirectOutput(OutputStream toOutput) {
        output = toOutput;
    }

    public void redirectError(OutputStream toOutput) {
        error = toOutput;
    }

    public void setDirectory(File procDir) {
        pb.directory(procDir);
    }

    /**
     * Launches a process which executes the handler's command. Does nothing if
     * the process has been launched already.
     * <p>
     * Sets {@link ProcessHandler#hasLaunched} to true on successful launch of
     * the process, making {@link ProcessHandler#hasLaunched()} return true
     * thereafter.
     * <p>
     * After launching, it sleeps for {@link ProcessHandler#startWait}
     * milliseconds.
     */
    public void launchProcess() {
        try {
            if (currentProcess == null) {
                hasLaunched = true;
                currentProcess = pb.start();
                if (output != null) inheritIO(currentProcess.getInputStream(), new PrintStream(output));
                if (error != null) inheritIO(currentProcess.getErrorStream(), new PrintStream(error));
                if (startWait > 0) Thread.sleep(startWait);
            } else {
                LOGGER.warn("Process has already been started");
            }

        } catch (IOException | InterruptedException E) {
            LOGGER.error("Couldn't start process due to exec:", E);
            throw new RuntimeException(E);
        }
    }

    /**
     * Terminates the process executing the handler's command. Does nothing if
     * the process has been terminated already.
     */
    public void terminateProcess() {
        if (currentProcess != null) {
            if (terminateCommand != null) {
                try {
                    // '+' after \\s takes care of multiple consecutive spaces
                    Runtime.getRuntime().exec(terminateCommand.split("\\s+"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                currentProcess.destroyForcibly();
            }
            currentProcess = null;
        } else {
            LOGGER.warn("Process has already been ended");
        }
    }

    public boolean isAlive() {
        return currentProcess != null && currentProcess.isAlive();
    }

    /**
     * Returns true if the process has been launched successfully at least once,
     * irrespective of whether it has terminated since first execution.
     */
    public boolean hasLaunched() {
        return hasLaunched;
    }

    protected void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(() -> {
            Scanner sc = new Scanner(src);
            while (sc.hasNextLine()) {
                dest.println(sc.nextLine());
            }
            sc.close();
        }).start();
    }

}
