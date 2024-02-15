package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import de.learnlib.oracle.MembershipOracle;
import net.automatalib.word.Word;

import java.util.HashMap;

/**
 * Interface for the test running process.
 */
public interface TestRunner {
    /**
     * Runs a single test multiple times against a specified sulOracle.
     *
     * @param <I>        the type of inputs
     * @param <O>        the type of outputs
     * @param test       the test to be run in an input word format
     * @param times      the number of times to repeat the test
     * @param sulOracle  the Oracle against which the test will be run
     * @return           the corresponding {@link TestRunnerResult}
     */
    static <I, O> TestRunnerResult<Word<I>, O> runTest(Word<I> test, int times, MembershipOracle<I, O> sulOracle) {
        HashMap<O, Integer> answerMap = new HashMap<>();

        for (int i = 0; i < times; i++) {
            O answer = sulOracle.answerQuery(test);
            if (!answerMap.containsKey(answer)) {
                answerMap.put(answer, 1);
            } else {
                answerMap.put(answer, answerMap.get(answer) + 1);
            }
        }

        return new TestRunnerResult<>(test, answerMap);
    }

    /**
     * Runs the implemented test runner.
     */
    public void run();
}
