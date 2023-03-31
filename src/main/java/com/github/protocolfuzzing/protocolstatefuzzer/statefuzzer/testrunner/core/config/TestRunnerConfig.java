package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

import com.beust.jcommander.Parameter;

public class TestRunnerConfig {
    @Parameter(names = "-test", description = "Debug option, instead of learning, executes a test in the given file "
            + "and exits. If the file doesn't exist, it assumes the string supplied is a space-separated sequence of "
            + "inputs. It parses and executed these inputs on the system.")
    protected String test = null;

    @Parameter(names = "-times", description = "The number of times the tests should be run")
    protected Integer times = 1;

    @Parameter(names = "-testSpecification", description = "A .dot model against which the resulting outputs are "
            + "compared. If provided, the test will be run both on the system and on the model.")
    protected String testSpecification = null;

    @Parameter(names = "-showTransitionSequence", description = "Show the sequence of transitions at the end in a "
            + "nicer form.")
    protected boolean showTransitionSequence = false;

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestSpecification() {
        return testSpecification;
    }

    public boolean isShowTransitionSequence() {
        return showTransitionSequence;
    }

}
