package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Caches inputs and outputs and adds functionality for terminating outputs.
 */
public class CachingSULOracle<I, O> implements MealyMembershipOracle<I, O> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected ObservationTree<I, O> cache;

    /** Stores the constructor parameter. */
    protected MembershipOracle<I, Word<O>> sulOracle;

    /** Stores the constructor parameter. */
    protected boolean onlyLookup;

    /** The set of terminating outputs specified in the constructor. */
    protected HashSet<O> terminatingOutputs;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sulOracle           the sul oracle to be wrapped
     * @param cache               the external cache used to lookup and store inputs and outputs
     * @param onlyLookup          {@code true} if the external cache is used only for lookup, but not for storing
     * @param terminatingOutputs  the terminating outputs to be used
     */
    public CachingSULOracle(MembershipOracle<I, Word<O>> sulOracle, ObservationTree<I, O> cache,
        boolean onlyLookup, List<O> terminatingOutputs) {

        this.cache = cache;
        this.sulOracle = sulOracle;
        this.onlyLookup = onlyLookup;
        this.terminatingOutputs = new HashSet<O>();

        if (terminatingOutputs != null && !terminatingOutputs.isEmpty()) {
            this.terminatingOutputs.addAll(terminatingOutputs);
        }
    }

    /**
     * Processes the given queries and provides them with their corresponding answer
     * using their {@link Query#answer(Object)} method.
     * <p>
     * It also stores each input and output to {@link #cache}, if storing is
     * enabled.
     *
     * @param queries  the queries to be answered
     */
    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> q : queries) {
            Word<I> fullInput = q.getPrefix().concat(q.getSuffix());
            Word<O> fullOutput = cacheAnswer(fullInput);

            if (fullOutput != null) {
                LOGGER.debug("CACHE HIT!");
            } else {
                fullOutput = sulOracle.answerQuery(fullInput);
                if (!onlyLookup) {
                    cacheAdd(fullInput, fullOutput);
                }
            }

            Word<O> output = fullOutput.suffix(q.getSuffix().size());
            q.answer(output);
        }
    }

    /**
     * Adds the input and output words to {@link #cache}.
     *
     * @param input   the input
     * @param output  the corresponding output
     */
    protected void cacheAdd(Word<I> input, Word<O> output) {
        try {
            cache.addObservation(input, output);
        } catch (CacheInconsistencyException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Looks up {@link #cache} for an output to the given input.
     *
     * @param input  the input to be answered if it is cached
     * @return       the corresponding output or null
     */
    @Nullable protected Word<O> cacheAnswer(Word<I> input) {
        if (terminatingOutputs.isEmpty()) {
            return cache.answerQuery(input);
        }

        Word<O> output = cache.answerQuery(input, true);

        if (output == null || output.isEmpty()) {
            return null;
        }

        if (output.length() >= input.length()) {
            return output;
        }

        if (terminatingOutputs.contains(output.lastSymbol())) {
            Word<O> extendedOutput = output;
            while (extendedOutput.length() < input.length()) {
                extendedOutput = extendedOutput.append(output.lastSymbol());
            }
            return extendedOutput;
        }

        return null;
    }
}
