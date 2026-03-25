package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import java.util.Map;

/**
 * Represents the result of a single test run.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class TestRunnerResult<I, O> {

    /** Stores the constructor parameter. */
    protected I inputWord;

    /** Stores the constructor parameter. */
    protected Map<O, Integer> generatedOutputs;

    /** Stores a single expected output. */
    protected O expectedOutputWord;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param inputWord         the test used for the run
     * @param generatedOutputs  the map of generated outputs to their frequency
     */
    public TestRunnerResult(I inputWord, Map<O, Integer> generatedOutputs) {
        this.inputWord = inputWord;
        this.generatedOutputs = generatedOutputs;
    }

    /**
     * Sets the expected output word.
     *
     * @param outputWord  the output word to be expected
     */
    public void setExpectedOutputWord(O outputWord) {
        expectedOutputWord = outputWord;
    }

    /**
     * Returns the input word (test) provided in the constructor.
     *
     * @return  the input word (test) provided in the constructor
     */
    public I getInputWord() {
        return inputWord;
    }

    /**
     * Returns the map provided in the constructor.
     *
     * @return  the map provided in the constructor
     */
    public Map<O, Integer> getGeneratedOutputs() {
        return generatedOutputs;
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of this instance
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Test: ").append(inputWord).append(System.lineSeparator());

        for (Map.Entry<O, Integer> entry : generatedOutputs.entrySet()) {
            sb.append(entry.getValue())
                .append(" times outputs: ")
                .append(entry.getKey().toString())
                .append(System.lineSeparator());
        }

        if (expectedOutputWord != null) {
            sb.append("Expected output: ").append(expectedOutputWord);
        }

        return sb.toString();
    }
}
