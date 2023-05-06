package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

/**
 * Interface for an adapter used to communicate with a launch server
 * responsible for starting and stopping the SUL processes.
 */
public interface SulAdapter {

    /**
     * Connects to the launch server if not already connected.
     */
     void connect();

    /**
     * Asks the launch server to launch a new SUL process.
     */
     void start();

    /**
     * Asks the launch server to terminate the current SUL process.
     */
    void stop();

    /**
     * Checks if the launch server reported that the SUL process has terminated.
     *
     * @return  {@code true} if the previously running SUL process has terminated
     */
    boolean checkStopped();

    /**
     * Retrieves the local port of the running SUL process.
     *
     * @return  the local port of the running SUL process
     */
    Integer getSulPort();

    /**
     * Returns {@code true} if the launch server launches client SUL processes
     * and {@code false} if the launch server launches server SUL processes.
     *
     * @return  {@code true} if the launch server launches client SUL processes
     *          and {@code false} if the launch server launches server SUL processes
     */
    boolean isClientLauncher();
}
