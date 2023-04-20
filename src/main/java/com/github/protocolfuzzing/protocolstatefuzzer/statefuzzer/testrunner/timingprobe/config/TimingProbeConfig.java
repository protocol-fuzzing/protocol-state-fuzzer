package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

import com.beust.jcommander.Parameter;

/**
 * The configuration of the TimingProbe.
 */
public class TimingProbeConfig {

    /**
     * Stores the JCommander Parameter -probeCmd, -timingProbe.
     * <p>
     * It can be a single command or comma-separated commands.
     * Available commands are: timeout, runWait or an input from the given alphabet.
     */
    @Parameter(names = {"-probeCmd", "-timingProbe"}, description = "Probe for timing values by testing for non-determinism. "
        + "It can be a single command or comma-separated commands. "
        + "Available commands are: timeout, runWait or an input from the given alphabet.")
    protected String probeCmd = null;

    /**
     * Stores the JCommander Parameter -probeMin.
     * <p>
     * Minimum timing value for probe. Should be between {@link #probeLo} and {@link #probeHi}.
     */
    @Parameter(names = "-probeMin", description = "Minimum timing value for probe")
    protected Integer probeMin = 10;

    /**
     * Stores the JCommander Parameter -probeLow.
     * <p>
     * Lowest timing value for probe.
     */
    @Parameter(names = "-probeLow", description = "Lowest timing value for probe")
    protected Integer probeLo = 0;

    /**
     * Stores the JCommander Parameter -probeHigh.
     * <p>
     * Highest timing value for probe.
     */
    @Parameter(names = "-probeHigh", description = "Highest timing value for probe")
    protected Integer probeHi = 1000;

    /**
     * Stores the JCommander Parameter -probeExport.
     * <p>
     * Output file for the modified alphabet.
     */
    @Parameter(names = "-probeExport", description = "Output file for the modified alphabet")
    protected String probeExport = null;

    /**
     * Returns the value of {@link #probeCmd}.
     *
     * @return  the value of {@link #probeCmd}
     */
    public String getProbeCmd() {
        return probeCmd;
    }

    /**
     * Returns the value of {@link #probeMin}.
     *
     * @return  the value of {@link #probeMin}
     */
    public Integer getProbeMin() {
        return probeMin;
    }

    /**
     * Returns the value of {@link #probeLo}.
     *
     * @return  the value of {@link #probeLo}
     */
    public Integer getProbeLo() {
        return probeLo;
    }

    /**
     * Returns the value of {@link #probeHi}.
     *
     * @return  the value of {@link #probeHi}
     */
    public Integer getProbeHi() {
        return probeHi;
    }

    /**
     * Returns the value of {@link #probeExport}.
     *
     * @return  the value of {@link #probeExport}
     */
    public String getProbeExport() {
        return probeExport;
    }
}
