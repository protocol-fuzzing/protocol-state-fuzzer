package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.query.Query;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Writer;
import java.util.function.Supplier;

/**
 * Checks and confirms a potential counterexample by re-running it.
 * <p>
 * This allows to avoid restarting the whole testing process due to spurious counterexamples.
 * The output comparison should be done twice, but that cost is insignificant in the context of learning.
 *
 * @param <HA>  the type of the hypothesis automaton
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class CESanitizingSULOracle<HA extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, Word<O>>, I, O>
        extends MultipleRunsSULOracle<I, O> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected Supplier<HA> automatonProvider;

    /** Stores the constructor parameter. */
    protected boolean skipNonDetTests;

    /** Stores the constructor parameter. */
    protected ObservationTree<I, O> cache;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param runs                       the number of times that a counterexample should be run, stored in {@link #runs}
     * @param sulOracle                  the sul Oracle that is being wrapped
     * @param probabilisticSanitization  {@code true} to enable the probabilistic sanitization
     * @param writer                     the writer used to log results and information
     * @param automatonProvider          the provider of the hypothesis automaton
     * @param cache                      the external cache used for lookup
     * @param skipNonDetTests            {@code true} to skip non-deterministic tests and not throw an exception
     */
    public CESanitizingSULOracle(int runs,
        MealyMembershipOracle<I, O> sulOracle, boolean probabilisticSanitization,
        Writer writer, Supplier<HA> automatonProvider,
        ObservationTree<I, O> cache, boolean skipNonDetTests) {

        super(runs, sulOracle, probabilisticSanitization, writer);
        this.automatonProvider = automatonProvider;
        this.cache = cache;
        this.skipNonDetTests = skipNonDetTests;
    }

    /**
     * Processes the given query using counterexample sanitization.
     *
     * @param query  the query to be processed
     *
     * @throws NonDeterminismException  thrown from {@link #getCheckedOutput(Word, Word, Word)}
     */
    @Override
    public void processQuery(Query<I, Word<O>> query) throws NonDeterminismException {
        Word<O> originalOutput = sulOracle.answerQuery(query.getInput());
        Word<O> hypOutput = automatonProvider.get().computeOutput(query.getInput());
        Word<O> returnedOutput;

        if (!originalOutput.equals(hypOutput)) {
            // possible counterexample
            LOGGER.debug("Confirming potential counterexample by re-running it");
            returnedOutput = getCheckedOutput(query.getInput(), originalOutput, hypOutput);
        } else {
            // no counterexample
            returnedOutput = originalOutput;
        }

        if (!returnedOutput.equals(hypOutput)) {
            // possible counterexample, check it against the cache
            Word<O> outputFromCache = cache.answerQuery(query.getInput(), true);

            if (outputFromCache != null && !outputFromCache.equals(returnedOutput.prefix(outputFromCache.length()))) {
                printWriter.println("Output inconsistent with cache, discarding it and returning automaton output");
                printWriter.println("Input: " + query.getInput().prefix(outputFromCache.length()));
                printWriter.println("Spurious output: " + returnedOutput);
                printWriter.println("Cached output: " + outputFromCache);
                printWriter.flush();
                returnedOutput = hypOutput;
            }
        }

        query.answer(returnedOutput.suffix(query.getSuffix().length()));
    }

    /**
     * Reruns the input {@link #runs} times and compares the checked output with
     * the ones given in the parameters.
     * <p>
     * Specifically:
     * <ul>
     * <li> If the output does not equal none of originalOutput and hypOutput then it is a CE
     * <li> If the output does not equal the originalOutput, but equals the hypOutput then it is not a CE
     * </ul>
     *
     * @param input           the input to be run multiple times
     * @param originalOutput  the original output of the sulOracle
     * @param hypOutput       the hypothesis output obtained from {@link #automatonProvider}
     * @return                the checked output or the hypOutput if the checked output
     *                        can be found and {@link #skipNonDetTests} is enabled
     *
     * @throws NonDeterminismException  if the checked output cannot be found after multiple runs
     */
    protected Word<O> getCheckedOutput(Word<I> input, Word<O> originalOutput, Word<O> hypOutput) throws NonDeterminismException {
        try {
            Word<O> checkedOutput = super.getMultipleRunOutput(input);

            if (!checkedOutput.equals(originalOutput)) {
                printWriter.println("Output changed following CE verification");
                printWriter.println("Input: " + input);
                printWriter.println("Original output: " + originalOutput);
                printWriter.println("New output: " + checkedOutput);

                if (!checkedOutput.equals(hypOutput)) {
                    printWriter.println("New CE status: is a CE");
                } else {
                    printWriter.println("New CE status: is not a CE");
                }

                printWriter.flush();
            }

            return checkedOutput;

        } catch (NonDeterminismException e) {
            if (!skipNonDetTests) {
                throw e;
            }

            // skip the non-deterministic input
            printWriter.println("NonDeterminism in running input");
            printWriter.println(e);
            printWriter.println("Skipping: " + input);
            printWriter.flush();
            return hypOutput;
        }
    }
}
