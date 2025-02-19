package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import net.automatalib.exception.FormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The standard implementation of the TimingProbe interface.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 * @param <E>  the type of execution context
 */
public class TimingProbeStandard<I extends MapperInput<O, P, E>, O extends MapperOutput<O, P>, P, E> implements TimingProbe {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the TimingProbeConfig from the TimingProbeEnabler constructor parameter. */
    protected TimingProbeConfig timingProbeConfig;

    /** Stores the constructor parameter. */
    protected AlphabetBuilder<I> alphabetBuilder;

    /** Stores the ProbeTestRunner, which is created if {@link #isActive()}. */
    protected ProbeTestRunner<I, O, P, E> probeTestRunner = null;

    /**
     * Constructs a new instance from the given parameters.
     *
     * <p>
     * Invoke {@link #initialize()} afterwards.
     *
     * @param timingProbeEnabler  the configuration that enables testing with the timing probe
     * @param alphabetBuilder     the builder of the alphabet
     * @param sulBuilder          the builder of the sul
     * @param sulWrapper          the wrapper of the sul
     */
    public TimingProbeStandard(
        TimingProbeEnabler timingProbeEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SulBuilder<I, O, E> sulBuilder,
        SulWrapper<I, O, E> sulWrapper
    ) {
        this.timingProbeConfig = timingProbeEnabler.getTimingProbeConfig();
        this.alphabetBuilder = alphabetBuilder;

        if(isActive()) {
            this.probeTestRunner = new ProbeTestRunner<>(
                timingProbeEnabler, alphabetBuilder, sulBuilder, sulWrapper
            );
        }
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * <p>
     * It initializes the {@link #probeTestRunner} if the probe is active.
     *
     * @return  the same instance
     */
    public TimingProbeStandard<I, O, P, E> initialize() {
        if (isActive() && this.probeTestRunner != null) {
            this.probeTestRunner.initialize();
        }
        return this;
    }

    /**
     * Runs the timing probe test given that {@link #isActive()}, there is
     * a {@link #probeTestRunner} and {@link #isValid()}.
     */
    @Override
    public void run() {
        if (!isActive() || probeTestRunner == null || !isValid()) {
            return;
        }

        try {
            Map<String, Integer> bestTimes = findDeterministicTimesValues();
            LOGGER.info(TimingProbe.present(bestTimes));
            alphabetBuilder.exportAlphabetToFile(timingProbeConfig.getProbeExport(), probeTestRunner.getAlphabet());

        } catch (ProbeException | IOException | FormatException | AlphabetSerializerException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();

        } finally {
            probeTestRunner.terminate();
        }
    }

    /**
     * Finds the lowest values for each probe command supplied in {@link #timingProbeConfig}.
     * <p>
     * The search is done using a form of binary search by first setting all
     * parameters to the probe high value of {@link #timingProbeConfig}
     * and then finding the first value that leads to deterministic results.
     *
     * @return  the map from commands to lowest timing probe values
     *
     * @throws IOException     from {@link ProbeTestRunner#isNonDeterministic(boolean)} or
     *                         from {@link #findProbeLimitRange(String)}
     * @throws ProbeException  if non-determinism is found at max timing values
     */
    public Map<String, Integer> findDeterministicTimesValues() throws IOException, FormatException, ProbeException {
        Map<String, Integer> map = new HashMap<>();
        String[] cmds = timingProbeConfig.getProbeCmd().split(",", -1);

        for (String cmd : cmds) {
            setTimingParameter(cmd, timingProbeConfig.getProbeHi());
        }

        // do a control run, throw exception if non-deterministic
        if (probeTestRunner.isNonDeterministic(true)) {
            throw new ProbeException("Non-determinism at max timing values");
        }

        Integer bestTime;
        ProbeLimitRange probeLimitRange;

        for (String cmd : cmds) {
            probeLimitRange = findProbeLimitRange(cmd);

            if (probeLimitRange.isHiDeterministic()) {
                bestTime = probeLimitRange.getHi();
            } else {
                bestTime = binarySearch(cmd, probeLimitRange);
            }

            map.put(cmd, bestTime);
            setTimingParameter(cmd, bestTime);
        }

        return map;
    }

    /**
     * Checks if this TimingProbe is active.
     *
     * @return  {@code true} if there is at least one specified probe command in {@link #timingProbeConfig}
     */
    public boolean isActive() {
        return timingProbeConfig.getProbeCmd() != null;
    }

    /**
     * Checks the validity of each specified probe command in {@link #timingProbeConfig}.
     *
     * @return  {@code true} if all specified commands {@link #timingProbeConfig} are valid
     */
    public boolean isValid() {
        String[] cmds = timingProbeConfig.getProbeCmd().split(",", -1);

        for (String cmd : cmds) {
            if (!isValid(cmd)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks the validity of the provided command.
     *
     * @param cmd  the command to be checked
     * @return     {@code true} if the given command is valid
     */
    public boolean isValid(String cmd) {
        if (cmd.contentEquals("timeout") || cmd.contentEquals("runWait")) {
            return true;
        }

        // check if the command is an alphabet input
        for (I in : probeTestRunner.getAlphabet()) {
            if (in.toString().contentEquals(cmd)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the limit range according to the limits provided in {@link #timingProbeConfig}.
     * <p>
     * Specifically in the returned ProbeLimitRange:
     * <ul>
     * <li> hi is set to the first encountered deterministic value
     *      (starts from probeLo in {@link #timingProbeConfig} and doubles in each iteration)
     * <li> lo is set to the last encountered non-deterministic value
     * </ul>
     *
     * @param cmd  the command for which the limits will be found
     * @return     the ProbeLimitRange holding the range found. If a deterministic
     *             value for hi is found on the first try then this is reflected in
     *             the in the hiDeterministic variable of ProbeLimitRange
     *
     * @throws IOException  from {@link ProbeTestRunner#isNonDeterministic(boolean)}
     */
    protected ProbeLimitRange findProbeLimitRange(String cmd) throws IOException, FormatException {
        Integer probeLo = timingProbeConfig.getProbeLo();
        Integer probeHi = timingProbeConfig.getProbeHi();
        Integer probeMin = timingProbeConfig.getProbeMin();

        Integer lo = probeLo;
        Integer hi = probeLo;
        boolean keepSearching;

        if (cmd.contentEquals("timeout") && hi == 0) {
            keepSearching = true;
        } else {
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }

        if (!keepSearching) {
            return new ProbeLimitRange(lo, hi, true);
        }

        if (probeLo > 0) {
            hi = probeLo;
        } else {
            hi = probeMin;
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }

        while (keepSearching && hi < probeHi) {
            lo = hi;
            hi = hi * 2;
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }

        return new ProbeLimitRange(lo, hi, false);
    }

    /**
     * Refines the search for deterministic value by using a binary search with
     * the search interval being [lo, hi] variables in given ProbeLimitRange.
     * <p>
     * The condition of the search is {@code hi - lo > probeMin},
     * where probeMin is the one specified in {@link #timingProbeConfig}.
     *
     * @param cmd              the command for which the final value will be found
     * @param probeLimitRange  the  ProbeLimitRange holding the search interval
     * @return                 the found deterministic timing probe value
     *
     * @throws IOException  from {@link ProbeTestRunner#isNonDeterministic(boolean)}
     */
    protected Integer binarySearch(String cmd, ProbeLimitRange probeLimitRange) throws IOException, FormatException {
        Integer hi = probeLimitRange.getHi();
        Integer lo = probeLimitRange.getLo();
        Integer probeMin = timingProbeConfig.getProbeMin();
        Integer mid;

        while (hi - lo > probeMin) {
            mid = lo + (hi - lo) / 2;
            setTimingParameter(cmd, mid);

            if (probeTestRunner.isNonDeterministic(false)) {
                lo = mid;
            } else {
                hi = mid;
            }
        }

        return hi;
    }

    /**
     * Sets the timing parameter of the command to the given time; in order to
     * affect the next time the tests would run.
     * <p>
     * In case the command is an alphabet input, then the extendedWait parameter
     * of this input is set.
     *
     * @param cmd   the command, whose timing parameter will change
     * @param time  the time to be set
     */
    protected void setTimingParameter(String cmd, Integer time) {
        Long timeL = time == null ? Long.valueOf(0) : Long.valueOf(time);

        if (cmd.contentEquals("timeout")) {
            probeTestRunner.getSulConfig().setResponseWait(timeL);
        } else if (cmd.contentEquals("runWait")) {
            probeTestRunner.getSulConfig().setStartWait(timeL);
        } else {
            for (I in : probeTestRunner.getAlphabet()) {
                if (in.toString().contentEquals(cmd)) in.setExtendedWait(timeL);
            }
        }
    }

    /**
     * Holds the probe limit range found by {@link #findProbeLimitRange}.
     */
    protected static class ProbeLimitRange {

        /** Indicates that the hi value is already deterministic. */
        protected boolean hiDeterministic;

        /** The low value of the timing probe test. */
        protected Integer lo;

        /** The high value of the timing probe test. */
        protected Integer hi;

        /**
         * Constructs a new instance from the given parameters.
         *
         * @param lo               the low value of the range
         * @param hi               the high value of the range
         * @param hiDeterministic  {@code true} if the provided hi is known to be
         *                         a deterministic value
         */
        public ProbeLimitRange(Integer lo, Integer hi, boolean hiDeterministic) {
            this.lo = lo;
            this.hi = hi;
            this.hiDeterministic = hiDeterministic;
        }

        /**
         * Returns the stored value of {@link #lo}.
         *
         * @return  the stored value of {@link #lo}
         */
        public Integer getLo() {
            return lo;
        }

        /**
         * Returns the stored value of {@link #hi}.
         *
         * @return  the stored value of {@link #hi}
         */
        public Integer getHi() {
            return hi;
        }

        /**
         * Returns the stored value of {@link #hiDeterministic}.
         *
         * @return  the stored value of {@link #hiDeterministic}
         */
        public boolean isHiDeterministic() {
            return hiDeterministic;
        }
    }
}
