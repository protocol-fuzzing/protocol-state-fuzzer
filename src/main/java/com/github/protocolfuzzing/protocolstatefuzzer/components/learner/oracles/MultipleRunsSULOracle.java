package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import net.automatalib.words.Word;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A membership oracle which executes each query multiple times in order to
 * handle non-determinism. In case the runs result in different outputs, it can
 * perform probabilistic sanitization. This entails running the query many
 * times, and computing the answer with the highest likelihood.
 * <p>
 * If the likelihood is greater than a threshold the answer is returned,
 * otherwise an exception is thrown.
 * <p>
 * This oracle provides a foundation for other oracles which may want to re-run
 * queries.
 */
public class MultipleRunsSULOracle<I, O> implements MealyMembershipOracle<I, O> {

    // x times cereruns
    protected static final int PROBABILISTIC_MIN_MULTIPLIER = 2;
    protected static final int PROBABILISTIC_MAX_MULTIPLIER = 7;
    protected static final double ACCEPTABLE_PROBABILISTIC_THRESHOLD = 0.8;
    protected static final double PASSABLE_PROBABILISTIC_THRESHOLD = 0.4;
    protected MealyMembershipOracle<I, O> sulOracle;
    protected PrintWriter log;
    protected int runs;
    protected boolean probabilisticSanitization;

    public MultipleRunsSULOracle(int runs, MealyMembershipOracle<I, O> sulOracle, boolean probabilisticSanitization,
                                 Writer log) {
        this.sulOracle = sulOracle;
        this.runs = runs;
        this.probabilisticSanitization = probabilisticSanitization;
        this.log = new PrintWriter(log);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> q : queries) {
            processQuery(q);
        }
    }

    public void processQuery(Query<I, Word<O>> q) {
        Word<O> output = getMultipleRunOutput(q.getInput());
        q.answer(output.suffix(q.getSuffix().length()));
    }

    protected Word<O> getMultipleRunOutput(Word<I> input) {
        TestRunnerResult<I, O> result = TestRunner.runTest(input, runs, sulOracle);
        Iterator<Word<O>> outputIter = result.getGeneratedOutputs().keySet().iterator();
        Word<O> checkedOutput;

        if (result.getGeneratedOutputs().size() > 1) {
            log.println("Non determinism when running test multiple times");
            log.write(result.toString());
            log.flush();

            if (!probabilisticSanitization) {
                throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
            } else {
                checkedOutput = getProbabilisticOutput(input);
            }
        } else {
            checkedOutput = outputIter.next();
        }
        return checkedOutput;
    }

    protected Word<O> getProbabilisticOutput(Word<I> input) {
        log.println("Performing probabilistic sanitization");
        log.flush();

        LinkedHashMap<Word<O>, Integer> frequencyMap = new LinkedHashMap<>();
        for (int i = 0; i < runs * PROBABILISTIC_MAX_MULTIPLIER; i++) {

            // update frequency map
            Word<O> answer = sulOracle.answerQuery(input);
            if (!frequencyMap.containsKey(answer)) {
                frequencyMap.put(answer, 1);
            } else {
                frequencyMap.put(answer, frequencyMap.get(answer) + 1);
            }

            // after running enough tests, we can check whether we can return an acceptable answer
            if (i >= runs * PROBABILISTIC_MIN_MULTIPLIER) {
                Entry<Word<O>, Integer> mostCommonEntry =  frequencyMap.entrySet().stream()
                        .max(Entry.comparingByValue())
                        .orElseThrow();
                double likelihood = (double) mostCommonEntry.getValue() / (i + 1);

                log.println("Most likely answer has likelihood " + likelihood + " after " + (i + 1) + " runs");
                if (likelihood >= ACCEPTABLE_PROBABILISTIC_THRESHOLD) {
                    log.println("Answer deemed to be in acceptable range, returning answer");
                    log.flush();
                    return mostCommonEntry.getKey();
                } else {
                    if (likelihood >= PASSABLE_PROBABILISTIC_THRESHOLD) {
                        log.println("Answer deemed to be in passable range, continuing execution");
                    } else {
                        log.flush();
                        Iterator<Word<O>> outputIter = frequencyMap.keySet().iterator();
                        // TODO NonDeterminismException should carry multiple outputs
                        throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
                    }
                }
            }
        }

        log.flush();

        // we get here after exhausting the number of tests, without having found an answer that is acceptable
        Iterator<Word<O>> outputIter = frequencyMap.keySet().iterator();
        throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
    }

}
