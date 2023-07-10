package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config;

/**
 * The empty TimingProbe configuration without any JCommander Parameters.
 */
public class TimingProbeConfigEmpty implements TimingProbeConfig {

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getProbeCmd() {
        return null;
    }

    /**
     * Returns 10.
     *
     * @return  10
     */
    @Override
    public Integer getProbeMin() {
        return 10;
    }

    /**
     * Returns 0.
     *
     * @return  0
     */
    @Override
    public Integer getProbeLo() {
        return 0;
    }

    /**
     * Returns 1000.
     *
     * @return  1000
     */
    @Override
    public Integer getProbeHi() {
        return 1000;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getProbeExport() {
        return null;
    }
}
