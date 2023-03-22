package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * The role of the resetting wrapper is to communicate with a SUL wrapper over a
 * TCP connection. The resetting wrapper sends "reset" commands to the SUL
 * wrapper. The SUL wrapper reacts by terminating the current SUL instance,
 * starting a new one and responding with the fresh port number the instance
 * listens to. This response is also used as a form of acknowledgement, telling
 * the learning setup that the new instance is ready to receive messages.
 * <p>
 * Setting the port dynamically (rather than binding it statically) proved
 * necessary in order to avoid port collisions.
 */
public class ResettingServerWrapper<I, O> implements SUL<I, O>, DynamicPortProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    protected SUL<I, O> sul;

    protected Socket resetSocket;
    protected InetSocketAddress resetAddress;
    protected long resetCommandWait;
    protected Integer dynamicPort;
    protected BufferedReader reader;

    public ResettingServerWrapper(SUL<I, O> sul, SulConfig sulConfig, CleanupTasks tasks) {
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

    public Integer getSulPort() {
        return dynamicPort;
    }

    @Override
    public void pre() {
        try {
            if (!resetSocket.isConnected()) {
                resetSocket.connect(resetAddress);
                reader = new BufferedReader(new InputStreamReader(resetSocket.getInputStream()));
            }
            byte[] resetCmd = "reset\n".getBytes();

            resetSocket.getOutputStream().write(resetCmd);
            resetSocket.getOutputStream().flush();
            String portString = reader.readLine();
            if (portString == null) {
                throw new RuntimeException("Server has closed the socket");
            }

            dynamicPort = Integer.valueOf(portString);
            if (resetCommandWait > 0) {
                Thread.sleep(resetCommandWait);
            }

            LOGGER.info("Server listening at port {}", portString);

            /*
             * We have to pre before the SUL does, so we have a port available
             * for it.
             */

            sul.pre();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void post() {
        sul.post();
    }

    @Override
    public O step(I in) throws SULException {
        return sul.step(in);
    }
}
