package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.word.Word;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Executes each query multiple times in order to handle non-determinism
 * and in case the runs result in different outputs it can perform probabilistic sanitization.
 * <p>
 * Probabilistic sanitization entails running the query many times and
 * computing the answer with the highest likelihood. If the likelihood is
 * greater than a threshold the answer is returned otherwise {@link NonDeterminismException} is thrown.
 * <p>
 * This oracle provides a foundation for other oracles that want to re-run queries.
 */
public class MultipleRunsSULOracle<I, O> implements MealyMembershipOracle<I, O> {

    /** Minimum multiplier for {@link #runs} used in probabilistic sanitization.*/
    protected static final int PROBABILISTIC_MIN_MULTIPLIER = 2;

    /** Maximum multiplier for {@link #runs} used in probabilistic sanitization.*/
    protected static final int PROBABILISTIC_MAX_MULTIPLIER = 7;

    /** Acceptable threshold percentage for an answer after multiple runs. */
    protected static final double ACCEPTABLE_PROBABILISTIC_THRESHOLD = 0.8;

    /** Passable threshold percentage (less than acceptable) for an answer after multiple runs. */
    protected static final double PASSABLE_PROBABILISTIC_THRESHOLD = 0.4;

    /** Stores the constructor parameter. */
    protected MealyMembershipOracle<I, O> sulOracle;

    /** Stores the Writer constructor parameter wrapped it with a PrintWriter. */
    protected PrintWriter printWriter;

    /** Stores the constructor parameter. */
    protected int runs;

    /** Stores the constructor parameter. */
    protected boolean probabilisticSanitization;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param runs                       the number of times that a query should be run
     * @param sulOracle                  the sul Oracle that is being wrapped
     * @param probabilisticSanitization  {@code true} to enable the probabilistic sanitization
     * @param writer                     the writer used to log results and information
     */
    public MultipleRunsSULOracle(int runs, MealyMembershipOracle<I, O> sulOracle, boolean probabilisticSanitization, Writer writer) {
        this.sulOracle = sulOracle;
        this.runs = runs;
        this.probabilisticSanitization = probabilisticSanitization;
        this.printWriter = new PrintWriter(writer);
    }

    /**
     * Processes queries using {@link #processQuery}.
     *
     * @param queries  the queries to be processed
     */
    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> query : queries) {
            processQuery(query);
        }
    }

    /**
     * Processes the given query using {@link #getMultipleRunOutput}.
     *
     * @param query  the query to be processed
     */
    @Override
    public void processQuery(Query<I, Word<O>> query) {
        Word<O> output = getMultipleRunOutput(query.getInput());
        query.answer(output.suffix(query.getSuffix().length()));
    }

    /**
     * Runs an input {@link #runs} times and also performs probabilistic sanitization if
     * multiple different answers are received and {@link #probabilisticSanitization} enables it.
     *
     * @param input  the input to be used
     * @return       the single output that corresponds to the input
     *
     * @throws NonDeterminismException  if multiple different answers are received
     *                                  and probabilistic sanitization is disabled
     *                                  or if probabilistic sanitization is performed
     *                                  but fails to find an answer
     */
    protected Word<O> getMultipleRunOutput(Word<I> input) throws NonDeterminismException {
        TestRunnerResult<I, O> result = TestRunner.runTest(input, runs, sulOracle);
        Iterator<Word<O>> outputIter = result.getGeneratedOutputs().keySet().iterator();

        if (result.getGeneratedOutputs().size() > 1) {
            printWriter.println("Non determinism when running test multiple times");
            printWriter.write(result.toString());
            printWriter.flush();

            if (!probabilisticSanitization) {
                throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
            }

            // use probabilistic sanitization
            return getProbabilisticOutput(input);
        }

        // single output returned
        return outputIter.next();
    }

    /**
     * Runs an input many times and returns the most probable answer.
     * <p>
     * Specifically:
     * <ul>
     * <li> Runs the input at most {@link #runs} * {@link #PROBABILISTIC_MAX_MULTIPLIER} times
     * <li> Checks the likelihood of the most common answer every time after {@link #runs} * {@link #PROBABILISTIC_MIN_MULTIPLIER} times
     * <li> If the most common answer has likelihood over {@link #ACCEPTABLE_PROBABILISTIC_THRESHOLD} then it is returned
     * <li> But if the most common answer has likelihood only over {@link #PASSABLE_PROBABILISTIC_THRESHOLD} then the process continues
     * </ul>
     *
     * @param input  the input to be used
     * @return       the single output that corresponds to the input
     *
     * @throws NonDeterminismException  if no acceptable answer can be found or
     *                                  if the most common answer has likelihood (anytime)
     *                                  below {@link #PASSABLE_PROBABILISTIC_THRESHOLD}
     */
    protected Word<O> getProbabilisticOutput(Word<I> input) throws NonDeterminismException {
        printWriter.println("Performing probabilistic sanitization");
        printWriter.flush();

        LinkedHashMap<Word<O>, Integer> frequencyMap = new LinkedHashMap<>();

        for (int i = 0; i < runs * PROBABILISTIC_MAX_MULTIPLIER; i++) {
            Word<O> answer = sulOracle.answerQuery(input);

            // update frequency map
            if (!frequencyMap.containsKey(answer)) {
                frequencyMap.put(answer, 1);
            } else {
                frequencyMap.put(answer, frequencyMap.get(answer) + 1);
            }

            // after running enough tests, we can check whether we can return an acceptable answer
            if (i >= runs * PROBABILISTIC_MIN_MULTIPLIER) {
                Entry<Word<O>, Integer> mostCommonEntry =  frequencyMap.entrySet().stream().max(Entry.comparingByValue()).orElseThrow();
                double likelihood = (double) mostCommonEntry.getValue() / (i + 1);
                printWriter.println("Most likely answer has likelihood " + likelihood + " after " + (i + 1) + " runs");

                if (likelihood >= ACCEPTABLE_PROBABILISTIC_THRESHOLD) {
                    printWriter.println("Answer deemed to be in acceptable range, returning answer");
                    printWriter.flush();
                    return mostCommonEntry.getKey();
                }

                if (likelihood >= PASSABLE_PROBABILISTIC_THRESHOLD) {
                    printWriter.println("Answer deemed to be in passable range, continuing execution");
                    continue;
                }

                // Neither in acceptable not in passable range
                printWriter.flush();
                Iterator<Word<O>> outputIter = frequencyMap.keySet().iterator();
                throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
            }
        }

        // TODO NonDeterminismException should carry multiple outputs
        // exhausted the number of tests, without having found an acceptable answer
        printWriter.flush();
        Iterator<Word<O>> outputIter = frequencyMap.keySet().iterator();
        throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
    }
}
