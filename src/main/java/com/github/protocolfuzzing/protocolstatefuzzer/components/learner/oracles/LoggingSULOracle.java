package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Logs the queries that it processes to the console and optionally using
 * a given Writer.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class LoggingSULOracle<I, O> implements MealyMembershipOracle<I, O> {
    private static final Logger LOGGER = LogManager.getLogger();

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
        this.printWriter = writer == null ? null : new PrintWriter(writer);
    }

    /**
     * Constructs a new instance from the given parameter without any Writer.
     *
     * @param sulOracle  the sul Oracle that is being wrapped
     */
    public LoggingSULOracle(MealyMembershipOracle<I, O> sulOracle) {
        this.sulOracle = sulOracle;
        this.printWriter = null;
    }

    /**
     * Processes the provided query using {@link processQueries} and
     * logs the query.
     *
     * @param query  the query to be processed
     */
    @Override
    public void processQuery(Query<I, Word<O>> query) {
        sulOracle.processQuery(query);
        LOGGER.debug(query.toString() + System.lineSeparator());
    }

    /**
     * Processes the provided queries using the stored {@link #sulOracle} and then
     * logs each query (with their answer) using {@link #printWriter}.
     *
     * @param queries  the queries to be processed
     */
    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> query : queries) {
            sulOracle.processQuery(query);

            if (printWriter != null) {
                printWriter.println(query.toString());
            }
        }

        if (printWriter != null) {
            printWriter.flush();
        }
    }
}
