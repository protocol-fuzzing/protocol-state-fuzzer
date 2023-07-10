package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.beust.jcommander.Parameter;

/**
 * The standard TestRunner configuration.
 */
public class TestRunnerConfigStandard implements TestRunnerConfig {

    /**
     * Stores the JCommander Parameter -test.
     * <p>
     * This is the option that enables testing and should either point to a file that
     * contains tests or should be a string of space-separated sequence of
     * inputs that represent a test.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-test", description = "This is the option that enables testing and "
            + "should either point to a file that contains tests or should be a string "
            + "space-separated sequence of inputs that represent a test.")
    protected String test = null;

    /**
     * Stores the JCommander Parameter -times.
     * <p>
     * The number of times the tests should be run.
     * <p>
     * Default value: 1.
     */
    @Parameter(names = "-times", description = "The number of times the tests should be run")
    protected Integer times = 1;

    /**
     * Stores the JCommander Parameter -testSpecification.
     * <p>
     * A DOT model against which the resulting outputs are compared.
     * If provided, the test will be run both on the system and on the model.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-testSpecification", description = "A DOT model against which the resulting outputs are "
            + "compared. If provided, the test will be run both on the system and on the model.")
    protected String testSpecification = null;

    /**
     * Stores the JCommander Parameter -showTransitionSequence.
     * <p>
     * Show the sequence of transitions at the end in a nicer form.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-showTransitionSequence", description = "Show the sequence of transitions at the end in a "
            + "nicer form.")
    protected boolean showTransitionSequence = false;

    /**
     * Returns the value of {@link #test}.
     *
     * @return  the value of {@link #test}
     */
    @Override
    public String getTest() {
        return test;
    }

    /**
     * Returns the value of {@link #times}.
     *
     * @return  the value of {@link #times}
     */
    @Override
    public Integer getTimes() {
        return times;
    }

    /**
     * Returns the value of {@link #testSpecification}.
     *
     * @return  the value of {@link #testSpecification}
     */
    @Override
    public String getTestSpecification() {
        return testSpecification;
    }

    /**
     * Returns the value of {@link #showTransitionSequence}.
     *
     * @return  the value of {@link #showTransitionSequence}
     */
    @Override
    public boolean isShowTransitionSequence() {
        return showTransitionSequence;
    }
}
