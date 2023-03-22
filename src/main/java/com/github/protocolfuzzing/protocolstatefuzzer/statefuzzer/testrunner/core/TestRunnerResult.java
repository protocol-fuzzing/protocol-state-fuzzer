package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import net.automatalib.words.Word;

import java.util.Map;

/**
 * Result for a single test
 */
public class TestRunnerResult<I, O> {
    protected Word<I> inputWord;
    protected Map<Word<O>, Integer> generatedOutputs;
    protected Word<O> expectedOutputWord;

    public TestRunnerResult(Word<I> inputWord,
                            Map<Word<O>, Integer> generatedOutputs) {
        super();
        this.inputWord = inputWord;
        this.generatedOutputs = generatedOutputs;
    }

    public void setExpectedOutputWord(Word<O> outputWord) {
        expectedOutputWord = outputWord;
    }

    public Word<I> getInputWord() {
        return inputWord;
    }

    public Map<Word<O>, Integer> getGeneratedOutputs() {
        return generatedOutputs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Test: ").append(inputWord).append(System.lineSeparator());
        for (Word<O> answer : generatedOutputs.keySet()) {
            sb.append(generatedOutputs.get(answer)).append(" times outputs: ")
                    .append(answer.toString()).append(System.lineSeparator());
        }

        if (expectedOutputWord != null) {
            sb.append("Expected output: ").append(expectedOutputWord);
        }

        return sb.toString();
    }
}
