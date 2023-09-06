package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

import java.io.Writer;

/**
 * Checks and confirms a potential non-deterministic answer by re-running it.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class NonDeterminismRetryingSULOracle<I, O> extends MultipleRunsSULOracle<I, O> {

    /** Stores the constructor parameter. */
    protected ObservationTree<I, O> cache;

    /** Stores the preceding input of the current query under processing. */
    protected Word<I> precedingInput;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param runs                       the number of times that a query should be run
     * @param sulOracle                  the sul Oracle that is being wrapped
     * @param probabilisticSanitization  {@code true} to enable the probabilistic sanitization
     * @param writer                     the writer used to log results and information
     * @param cache                      the external cache used for lookup
     */
    public NonDeterminismRetryingSULOracle(int runs,
        MealyMembershipOracle<I, O> sulOracle, boolean probabilisticSanitization,
        Writer writer, ObservationTree<I, O> cache) {

        super(runs, sulOracle, probabilisticSanitization, writer);
        this.cache = cache;
    }

    /**
     * Processes the given query by comparing the {@link #sulOracle}'s answer with
     * the cached one and if they differ then {@link #getCheckedOutput(Word, Word)} is used.
     *
     * @param query  the query to be processed
     *
     * @throws NonDeterminismException  thrown from {@link #getCheckedOutput(Word, Word)}
     */
    @Override
    public void processQuery(Query<I, Word<O>> query) throws NonDeterminismException {
        Word<O> originalOutput = sulOracle.answerQuery(query.getInput());
        Word<O> outputFromCache = cache.answerQuery(query.getInput(), true);
        Word<O> returnedOutput = originalOutput;

        if (outputFromCache != null && !outputFromCache.equals(originalOutput.prefix(outputFromCache.length()))) {
            printWriter.println("Output inconsistent with cache, rerunning membership query");
            printWriter.println("Input: " + query.getInput().prefix(outputFromCache.length()));
            printWriter.println("Unexpected output: " + returnedOutput);
            printWriter.println("Cached output: " + outputFromCache);
            printWriter.flush();

            try {
                returnedOutput = getCheckedOutput(query.getInput(), originalOutput);
            } catch (NonDeterminismException e) {
                e.setPrecedingInput(precedingInput);
                throw e;
            }
        }

        query.answer(returnedOutput.suffix(query.getSuffix().length()));
        precedingInput = query.getInput();
    }

    /**
     * Reruns the input {@link #runs} times and compares the checked output with
     * the given originalOutput.
     *
     * @param input           the input to be run multiple times
     * @param originalOutput  the original output of the sulOracle
     * @return                the checked output
     *
     * @throws NonDeterminismException  if the checked output cannot be found after multiple runs
     */
    protected Word<O> getCheckedOutput(Word<I> input, Word<O> originalOutput) throws NonDeterminismException {
        Word<O> checkedOutput = super.getMultipleRunOutput(input);

        if (!checkedOutput.equals(originalOutput)) {
            printWriter.println("Output changed following rerun");
            printWriter.println("Input: " + input);
            printWriter.println("Original output: " + originalOutput);
            printWriter.println("New output: " + checkedOutput);
            printWriter.flush();
        }
        return checkedOutput;
    }
}
