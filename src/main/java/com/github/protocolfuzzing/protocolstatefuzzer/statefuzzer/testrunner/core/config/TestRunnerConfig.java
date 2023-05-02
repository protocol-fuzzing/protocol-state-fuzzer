package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.beust.jcommander.Parameter;

/**
 * The configuration of the TestRunner.
 */
public class TestRunnerConfig {

    /**
     * Stores the JCommander Parameter -test.
     * <p>
     * This is the option that enables testing and should point to a file that
     * contains tests. Instead of a file, a string can be provided with
     * space-separated sequence of inputs that represent a test.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-test", description = "This is the option that enables testing and "
            + "should point to a file that contains tests. Instead of a file, "
            + "a string can be provided with space-separated sequence of inputs "
            + "that represent a test.")
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
     * Returns the value of {@link #times}.
     *
     * @return  the value of {@link #times}
     */
    public Integer getTimes() {
        return times;
    }

    /**
     * Sets the value of {@link #times}.
     *
     * @param times  the value of times to be set
     */
    public void setTimes(Integer times) {
        this.times = times;
    }

    /**
     * Returns the value of {@link #test}.
     *
     * @return  the value of {@link #test}
     */
    public String getTest() {
        return test;
    }

    /**
     * Sets the value of {@link #test}.
     *
     * @param test  the value of test to be set
     */
    public void setTest(String test) {
        this.test = test;
    }

    /**
     * Returns the value of {@link #testSpecification}.
     *
     * @return  the value of {@link #testSpecification}
     */
    public String getTestSpecification() {
        return testSpecification;
    }

    /**
     * Returns the value of {@link #showTransitionSequence}.
     *
     * @return  the value of {@link #showTransitionSequence}
     */
    public boolean isShowTransitionSequence() {
        return showTransitionSequence;
    }

}
