package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.EquivalenceOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for the StateFuzzerComposer being responsible for the setup of
 * the components of the state fuzzing process.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <D>  the type of output domain
 */
public interface StateFuzzerComposer<I, O, D> {

    /** The filename, where the non-determinism example will be stored, if it occurs. */
    String NON_DET_FILENAME = "nondet.log";

    /** The filename, where the queries will be logged, if query logging is enabled. */
    String QUERY_FILENAME = "query.log";

    /**
     * Returns the StatisticsTracker that will be used during the state fuzzing.
     *
     * @return  the StatisticsTracker that will be used during the state fuzzing
     */
    StatisticsTracker<I, O, D> getStatisticsTracker();

    /**
     * Returns the Learner that will be used during the state fuzzing.
     *
     * @return  the Learner that will be used during the state fuzzing
     */
    LearningAlgorithm.MealyLearner<I, O> getLearner();

    /**
     * Returns the Equivalence Oracle that will be used during the state fuzzing.
     *
     * @return  the Equivalence Oracle that will be used during the state fuzzing
     */
    EquivalenceOracle<MealyMachine<?, I, ?, O>, I, D>
    getEquivalenceOracle();

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
