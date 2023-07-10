package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

/**
 * Interface regarding the TestRunner configuration.
 */
public interface TestRunnerConfig {

    /**
     * Returns null or a single test string or a file that contains tests.
     * <p>
     * If the result is null, then testing cannot proceed.
     *
     * @return  null or a single test string or a file that contains tests
     */
    String getTest();

    /**
     * Returns the number of times the tests should be run.
     *
     * @return  the number of times the tests should be run
     */
    Integer getTimes();

    /**
     * Returns null or the path of a DOT model against which the resulting outputs
     * should be compared.
     *
     * @return  null or the path of a DOT model
     */
    String getTestSpecification();

    /**
     * Indicates whether to show the sequence of transitions at the end in a nicer form.
     *
     * @return  {@code true} if the sequence of transitions should be shown at
     * the end in a nicer form.
     */
    boolean isShowTransitionSequence();
}
