package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Logs the queries that it processes.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class LoggingSULOracle<I, O> implements MealyMembershipOracle<I, O> {

    /** Stores the constructor parameter. */
    protected MealyMembershipOracle<I, O> sulOracle;

    /** Stores the Writer constructor parameter wrapped with a PrintWriter. */
    protected PrintWriter printWriter;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sulOracle  the sul Oracle that is being wrapped
     * @param writer     the Writer used for logging queries
     */
    public LoggingSULOracle(MealyMembershipOracle<I, O> sulOracle, Writer writer) {
        this.sulOracle = sulOracle;
        this.printWriter = new PrintWriter(writer);
    }

    /**
     * Processes the provided queries using the stored {@link #sulOracle} and then
     * logs each query (with their answer) using {@link #printWriter}.
     *
     * @param queries  the queries to be processed
     */
    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        sulOracle.processQueries(queries);
        queries.forEach(q -> printWriter.println(q.toString()));
        printWriter.flush();
    }
}
