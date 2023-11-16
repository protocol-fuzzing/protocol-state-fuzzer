package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import net.automatalib.word.Word;

import java.util.Map;

/**
 * Represents the result of a single test run.
 */
public class TestRunnerResult<I, O> {

    /** Stores the constructor parameter. */
    protected Word<I> inputWord;

    /** Stores the constructor parameter. */
    protected Map<Word<O>, Integer> generatedOutputs;

    /** Stores a single expected output. */
    protected Word<O> expectedOutputWord;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param inputWord         the test used for the run
     * @param generatedOutputs  the map of generated outputs to their frequency
     */
    public TestRunnerResult(Word<I> inputWord, Map<Word<O>, Integer> generatedOutputs) {
        this.inputWord = inputWord;
        this.generatedOutputs = generatedOutputs;
    }

    /**
     * Sets the expected output word.
     *
     * @param outputWord  the output word to be expected
     */
    public void setExpectedOutputWord(Word<O> outputWord) {
        expectedOutputWord = outputWord;
    }

    /**
     * Returns the input word (test) provided in the constructor.
     *
     * @return  the input word (test) provided in the constructor
     */
    public Word<I> getInputWord() {
        return inputWord;
    }

    /**
     * Returns the map provided in the constructor.
     *
     * @return  the map provided in the constructor
     */
    public Map<Word<O>, Integer> getGeneratedOutputs() {
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

        for (Map.Entry<Word<O>, Integer> entry : generatedOutputs.entrySet()) {
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
