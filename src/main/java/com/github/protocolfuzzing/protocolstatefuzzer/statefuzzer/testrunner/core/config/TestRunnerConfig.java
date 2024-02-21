package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

/**
 * Interface regarding the TestRunner configuration.
 */
public interface TestRunnerConfig {

    /**
     * Returns null or a single test string or a file that contains tests.
     * <p>
     * If the result is null, then testing cannot proceed.
     * <p>
     * Default value: null.
     *
     * @return  null or a single test string or a file that contains tests
     */
    default String getTest() {
        return null;
    }

    /**
     * Returns the number of times the tests should be run.
     * <p>
     * Default value: 1.
     *
     * @return  the number of times the tests should be run
     */
    default Integer getTimes() {
        return 1;
    }

    /**
     * Returns null or the path of a DOT model against which the resulting outputs
     * should be compared.
     * <p>
     * Default value: null.
     *
     * @return  null or the path of a DOT model
     */
    default String getTestSpecification() {
        return null;
    }

    /**
     * Indicates whether to show the sequence of transitions at the end in a nicer form.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the sequence of transitions should be shown at
     *          the end in a nicer form.
     */
    default boolean isShowTransitionSequence() {
        return false;
    }
}
