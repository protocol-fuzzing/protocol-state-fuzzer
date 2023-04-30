package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.api.SUL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Communicates with a SUL client wrapper over a TCP connection.
 * <p>
 * It sends "reset" commands to the SUL wrapper. The SUL wrapper reacts by
 * terminating the current SUL instance, starting a new one and responding
 * with the fresh port number the instance listens to.
 * This response is also used as a form of acknowledgement, telling
 * the learning setup that the new instance is ready to receive messages.
 * <p>
 * Setting the port dynamically (rather than binding it statically) is
 * necessary in order to avoid port collisions.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class ResettingClientWrapper<I, O> implements SUL<I, O> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the socket used for the reset. */
    protected Socket resetSocket;

    /** Stores the address used for the reset. */
    protected InetSocketAddress resetAddress;

    /** Stores the wait time specified in {@link SulConfig#getResetCommandWait()}. */
    protected long resetCommandWait;

    /** Stores the reader of the {@link #resetSocket}. */
    protected BufferedReader resetSocketReader;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul        the inner sul to be wrapped
     * @param sulConfig  the configuration of the sul
     * @param tasks      the cleanup tasks to be executed in the end
     */
    public ResettingClientWrapper(SUL<I, O> sul, SulConfig sulConfig, CleanupTasks tasks) {
        this.sul = sul;
        resetAddress = new InetSocketAddress(sulConfig.getResetAddress(), sulConfig.getResetPort());
        resetCommandWait = sulConfig.getResetCommandWait();

        try {
            resetSocket = new Socket();
            resetSocket.setReuseAddress(true);
            resetSocket.setSoTimeout(20000);
            tasks.submit(() -> {
                try {
                    if (!resetSocket.isClosed()) {
                        LOGGER.info("Closing socket");
                        resetSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs before each test; used for setup.
     */
    @Override
    public void pre() {
        try {
            if (!resetSocket.isConnected()) {
                if (resetCommandWait > 0) Thread.sleep(resetCommandWait);
                resetSocket.connect(resetAddress);
                resetSocketReader = new BufferedReader(new InputStreamReader(resetSocket.getInputStream()));
            }

            byte[] resetCmd = "reset\n".getBytes();

            resetSocket.getOutputStream().write(resetCmd);
            resetSocket.getOutputStream().flush();
            String ack = resetSocketReader.readLine();

            if (ack == null) {
                throw new RuntimeException("Server has closed the socket");
            }

            LOGGER.info("Client connecting");

            // We have to pre before the SUL does, so we have a port available for it
            sul.pre();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs after each test; used for shutdown.
     */
    @Override
    public void post() {
        sul.post();
    }

    /**
     * Propagates the inputs of a test to the inner {@link #sul}.
     *
     * @param in  the input of the test
     * @return    the corresponding output
     *
     * @throws de.learnlib.api.exception.SULException  from the step method of the {@link #sul}
     */
    @Override
    public O step(I in) {
        return sul.step(in);
    }
}
