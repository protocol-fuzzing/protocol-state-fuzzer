package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

/**
 * Interface regarding the TimingProbe configuration.
 */
public interface TimingProbeConfig {

    /**
     * Returns a single command or comma-separated commands.
     * <p>
     * Available commands are: timeout, runWait or an input from the given alphabet.
     * <p>
     * Default value: null.
     *
     * @return  a single command or comma-separated commands
     */
    default String getProbeCmd() {
        return null;
    }

    /**
     * Returns the minimum timing value of probe.
     * <p>
     * It should be between {@link #getProbeLo()} and {@link #getProbeHi()}.
     * <p>
     * Default value: 10.
     *
     * @return  the minimum timing value of probe
     */
    default Integer getProbeMin() {
        return 10;
    }

    /**
     * Returns the lowest timing value of probe.
     * <p>
     * Default value: 0.
     *
     * @return  the lowest timing value of probe
     */
    default Integer getProbeLo() {
        return 0;
    }

    /**
     * Returns the highest timing value of probe.
     * <p>
     * Default value: 1000.
     *
     * @return  the highest timing value of probe
     */
    default Integer getProbeHi() {
        return 1000;
    }

    /**
     * Returns the output file to store the modified alphabet.
     * <p>
     * Default value: null.
     *
     * @return  the output file to store the modified alphabet
     */
    default String getProbeExport() {
        return null;
    }
}
