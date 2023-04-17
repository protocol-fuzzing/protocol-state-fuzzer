package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.StatisticsTracker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for the StateFuzzerComposer being responsible for the setup of
 * the components of the state fuzzing process.
 */
public interface StateFuzzerComposer {

    /** The filename, where the non-determinism example will be stored, if it occurs. */
    String NON_DET_FILENAME = "nondet.log";

    /** The filename, where the queries will be logged, if query logging is enabled. */
    String QUERY_FILENAME = "query.log";

    /**
     * Returns the StatisticsTracker that will be used during the state fuzzing.
     *
     * @return  the StatisticsTracker that will be used during the state fuzzing
     */
    StatisticsTracker getStatisticsTracker();

    /**
     * Returns the Learner that will be used during the state fuzzing.
     *
     * @return  the Learner that will be used during the state fuzzing
     */
    LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> getLearner();

    /**
     * Returns the Equivalence Oracle that will be used during the state fuzzing.
     *
     * @return  the Equivalence Oracle that will be used during the state fuzzing
     */
    EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>>
    getEquivalenceOracle();

    /**
     * Returns the alphabet that will be used during the state fuzzing.
     *
     * @return  the alphabet that will be used during the state fuzzing
     */
    Alphabet<AbstractInput> getAlphabet();

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
