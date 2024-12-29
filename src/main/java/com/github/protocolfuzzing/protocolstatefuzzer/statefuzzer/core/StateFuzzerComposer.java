package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import net.automatalib.alphabet.Alphabet;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for the setup of the components of the state fuzzing process.
 *
 * @param <I>   the type of inputs
 * @param <ST>  the type of statistics tracker
 * @param <LE>  the type of learner
 * @param <EQ>  the type of equivalence oracle
 */
public interface StateFuzzerComposer<I, ST extends StatisticsTracker<?, ?, ?, ?>, LE, EQ> {

    /** The filename, where the non-determinism example will be stored, if it occurs. */
    String NON_DET_FILENAME = "nondet.log";

    /** The filename, where the queries will be logged, if query logging is enabled. */
    String QUERY_FILENAME = "query.log";

    /**
     * Returns the StatisticsTracker that will be used during the state fuzzing.
     *
     * @return  the StatisticsTracker that will be used during the state fuzzing
     */
    ST getStatisticsTracker();

    /**
     * Returns the Learner that will be used during the state fuzzing.
     *
     * @return  the Learner that will be used during the state fuzzing
     */
    LE getLearner();

    /**
     * Returns the Equivalence Oracle that will be used during the state fuzzing.
     *
     * @return  the Equivalence Oracle that will be used during the state fuzzing
     */
    EQ getEquivalenceOracle();

    /**
     * Returns the alphabet that will be used during the state fuzzing.
     *
     * @return  the alphabet that will be used during the state fuzzing
     */
    Alphabet<I> getAlphabet();

    /**
     * Returns an input stream of the provided file of the alphabet.
     *
     * @return  an input stream of the provided file of the alphabet
     */
    InputStream getAlphabetFileInputStream();

    /**
     * Returns the alphabet file extension.
     *
     * @return  the alphabet file extension
     */
    String getAlphabetFileExtension();

    /**
     * Returns the configuration that will enable the state fuzzing.
     *
     * @return  the configuration that will enable the state fuzzing
     */
    StateFuzzerEnabler getStateFuzzerEnabler();

    /**
     * Returns the output directory of this state fuzzing process.
     *
     * @return  the output directory of this state fuzzing process
     */
    File getOutputDir();

    /**
     * Returns the cleanup tasks registered with this composer.
     *
     * @return  the cleanup tasks registered with this composer
     */
    CleanupTasks getCleanupTasks();
}
