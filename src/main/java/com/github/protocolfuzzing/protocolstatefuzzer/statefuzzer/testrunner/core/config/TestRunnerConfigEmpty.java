package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config;

/**
 * The empty TestRunner configuration without any JCommander Parameters.
 */
public class TestRunnerConfigEmpty implements TestRunnerConfig {

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getTest() {
        return null;
    }

    /**
     * Returns 1.
     *
     * @return  1
     */
    @Override
    public Integer getTimes() {
        return 1;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getTestSpecification() {
        return null;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isShowTransitionSequence() {
        return false;
    }
}
