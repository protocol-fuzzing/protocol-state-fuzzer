package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

/**
 * Interface regarding the TimingProbe configuration.
 */
public interface TimingProbeConfig {

    /**
     * Returns a single command or comma-separated commands.
     * <p>
     * Available commands are: timeout, runWait or an input from the given alphabet.
     *
     * @return  a single command or comma-separated commands
     */
    String getProbeCmd();

    /**
     * Returns the minimum timing value of probe.
     * <p>
     * It should be between {@link #getProbeLo()} and {@link #getProbeHi()}.
     *
     * @return  the minimum timing value of probe
     */
    Integer getProbeMin();

    /**
     * Returns the lowest timing value of probe.
     *
     * @return  the lowest timing value of probe
     */
    Integer getProbeLo();

    /**
     * Returns the highest timing value of probe.
     *
     * @return  the highest timing value of probe
     */
    Integer getProbeHi();

    /**
     * Returns the output file to store the modified alphabet.
     *
     * @return  the output file to store the modified alphabet
     */
    String getProbeExport();
}
