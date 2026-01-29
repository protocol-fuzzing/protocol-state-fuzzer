package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

import com.beust.jcommander.Parameter;

/**
 * The standard TimingProbe configuration.
 */
public class TimingProbeConfigStandard implements TimingProbeConfig {

    /**
     * Stores the JCommander Parameter -probeCmd, -timingProbe.
     * <p>
     * It can be a single command or comma-separated commands.
     * Available commands are: responseWait, startWait or an input from the given alphabet.
     * <p>
     * Default value: null.
     */
    @Parameter(names = {"-probeCmd", "-timingProbe"}, description = "Probe for timing values by testing for non-determinism. "
        + "It can be a single command or comma-separated commands. "
        + "Available commands are: responseWait, startWait or an input from the given alphabet.")
    protected String probeCmd = null;

    /**
     * Stores the JCommander Parameter -probeLow.
     * <p>
     * Lowest non-negative timing value of probe.
     * Negative value is treated as zero.
     * <p>
     * Default value: 0.
     */
    @Parameter(names = "-probeLow", description = "Lowest timing value of probe")
    protected Integer probeLo = 0;

    /**
     * Stores the JCommander Parameter -probeHigh.
     * <p>
     * Highest timing value of probe.
     * <p>
     * Default value: 1000.
     */
    @Parameter(names = "-probeHigh", description = "Highest timing value of probe")
    protected Integer probeHi = 1000;

    /**
     * Stores the JCommander Parameter -probeTol.
     * <p>
     * Search tolerance value that defines the desired precision.
     * The search stops once the range between high and low bounds is smaller than this value.
     * Small tolerance values increase accuracy but may require more iterations.
     * <p>
     * Default value: 10.
     */
    @Parameter(names = "-probeTol", description = "Search tolerance value of probe")
    protected Integer probeTol = 10;

    /**
     * Stores the JCommander Parameter -probeExport.
     * <p>
     * The output file to store the modified alphabet.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-probeExport", description = "The output file to store the modified alphabet")
    protected String probeExport = null;

    /**
     * Constructor
     */
    public TimingProbeConfigStandard() { }

    /**
     * Returns the value of {@link #probeCmd}.
     *
     * @return  the value of {@link #probeCmd}
     */
    @Override
    public String getProbeCmd() {
        return probeCmd;
    }

    /**
     * Returns the value of {@link #probeLo}.
     *
     * @return  the value of {@link #probeLo}
     */
    @Override
    public Integer getProbeLo() {
        return probeLo;
    }

    /**
     * Returns the value of {@link #probeHi}.
     *
     * @return  the value of {@link #probeHi}
     */
    @Override
    public Integer getProbeHi() {
        return probeHi;
    }

    /**
     * Returns the value of {@link #probeTol}.
     *
     * @return  the value of {@link #probeTol}
     */
    @Override
    public Integer getProbeTol() {
        return probeTol;
    }

    /**
     * Returns the value of {@link #probeExport}.
     *
     * @return  the value of {@link #probeExport}
     */
    @Override
    public String getProbeExport() {
        return probeExport;
    }
}
