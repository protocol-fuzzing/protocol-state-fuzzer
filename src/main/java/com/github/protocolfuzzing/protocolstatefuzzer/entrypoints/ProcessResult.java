package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;

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

    /** Stores the result of state fuzzing or testing, null if not applicable */
    private final LearnerResult<M> learnerResult;

    /** Stores the result of differential testing, null if not applicable */
    private final DiffTestResult difftestResult;

    /**
     * Constructs a new instance with the given result members.
     *
     * @param learnerResult  the result of state fuzzing or testing
     * @param diffTestResult the result of differential testing
     */
    private ProcessResult(LearnerResult<M> learnerResult, DiffTestResult diffTestResult) {
        this.learnerResult = learnerResult;
        this.difftestResult = diffTestResult;
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
        return new ProcessResult<>(result, null);
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
        return new ProcessResult<>(null, result);
    }

    /**
     * Returns the result of state fuzzing or testing.
     * <p>
     * Deafult value: null if the command was not satte fuzzing or testing.
     *
     * @return the result of state fuzzing or testing
     */
    public LearnerResult<M> getLearnerResult() {
        return learnerResult;
    }

    /**
     * Returns the result of differential testing.
     * <p>
     * Deafult value: null if the command was not differential testing.
     *
     * @return the result of differential testing
     */
    public DiffTestResult getDiffTestResult() {
        return difftestResult;
    }
}
