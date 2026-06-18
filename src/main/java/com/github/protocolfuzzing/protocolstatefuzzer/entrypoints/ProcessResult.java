package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;

/**
 * Used to store the result of any command executed by the {@link CommandLineParser}.
 * <p>
 * Exactly one of the result members is populated depending on the command that was executed.
 * The populated member can be determined using the corresponding {@code get} methods,
 * an unpopulatd member returns null. Use {@link #ofLearner(LearnerResult)} to
 * create an instance from a learning result and {@link #ofDiffTest(DiffTestResult)}
 * to create an instance from a differential testing result.
 *
 * @param <M> the type of machine model
 */
public class ProcessResult<M> {

    /** Stores the result of state fuzzing, testing or identifying, null if not applicable */
    private final LearnerResult<M> learnerResult;

    /** Stores the result of differential testing, null if not applicable */
    private final DiffTestResult diffTestResult;

    /** Stores the result of fingerprint extraction, null if not applicable */
    private final FingerprintNode fingerprintResult;

    /**
     * Constructs a new instance with the given result members.
     *
     * @param learnerResult  the result of state fuzzing or testing
     * @param diffTestResult the result of differential testing
     */
    private ProcessResult(LearnerResult<M> learnerResult, DiffTestResult diffTestResult,
        FingerprintNode fingerprintResult) {
        this.learnerResult = learnerResult;
        this.diffTestResult = diffTestResult;
        this.fingerprintResult = fingerprintResult;
    }

    /**
     * Returns a new empty instance where both inner results are null.
     *
     * @param  <M> the type of machine model
     *
     * @return     a new empty instance where both inner results are null
     */
    public static <M> ProcessResult<M> empty() {
        return new ProcessResult<>(null, null, null);
    }

    /**
     * Returns a new instance with the given LearnerResult populated.
     *
     * @param  <M>    the type of machine model
     * @param  result the result of state fuzzing or testing
     *
     * @return        a new instance with the learner result populated
     */
    public static <M> ProcessResult<M> ofLearner(LearnerResult<M> result) {
        return new ProcessResult<>(result, null, null);
    }

    /**
     * Returns a new instance with the given DiffTestResult populated.
     *
     * @param  <M>    the type of machine model
     * @param  result the result of differential testing
     *
     * @return        a new instance with the diff test result populated
     */
    public static <M> ProcessResult<M> ofDiffTest(DiffTestResult result) {
        return new ProcessResult<>(null, result, null);
    }

    /**
     * Returns a new instance with the given FingerprintNode populated.
     *
     * @param  <M>    the type of machine model
     * @param  result the result of fingerprinting
     *
     * @return        a new instance with the fingerprint result populated
     */
    public static <M> ProcessResult<M> ofFingerprint(FingerprintNode result) {
        return new ProcessResult<>(null, null, result);
    }

    /**
     * Returns the result of state fuzzing or testing.
     * <p>
     * Default value: null if the command was not satte fuzzing or testing.
     *
     * @return the result of state fuzzing or testing
     */
    public LearnerResult<M> getLearnerResult() {
        return learnerResult;
    }

    /**
     * Returns the result of differential testing.
     * <p>
     * Default value: null if the command was not differential testing.
     *
     * @return the result of differential testing
     */
    public DiffTestResult getDiffTestResult() {
        return diffTestResult;
    }

    /**
     * Returns the result of fingerprinting.
     * <p>
     * Default value: null if the command was not dingerprinting.
     *
     * @return the result of fingerprinting
     */
    public FingerprintNode getFingerprintResult() {
        return fingerprintResult;
    }

    /**
     * Returns true if the instance contains a learner result.
     *
     * @return true if a learner result is present, false otherwies
     */
    public boolean hasLearnerResult() {
        return learnerResult != null;
    }

    /**
     * Returns true if the instance contains a diff test result.
     *
     * @return true if a diff test result is present, false otherwies
     */
    public boolean hasDiffTestResult() {
        return diffTestResult != null;
    }

    /**
     * Returns true if the instance contains a fingerprint result.
     *
     * @return true if a fingerprint result is present, false otherwies
     */
    public boolean hasFingerprintResult() {
        return fingerprintResult != null;
    }
}
