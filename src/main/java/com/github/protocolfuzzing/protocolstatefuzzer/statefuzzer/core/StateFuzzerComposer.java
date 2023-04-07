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

public interface StateFuzzerComposer {

    String NON_DET_FILENAME = "nondet.log";
    String QUERY_FILENAME = "query.log";

    StatisticsTracker getStatisticsTracker();

    LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> getLearner();

    EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>>
    getEquivalenceOracle();

    Alphabet<AbstractInput> getAlphabet();

    InputStream getAlphabetFileInputStream();

    String getAlphabetFileExtension();

    StateFuzzerEnabler getStateFuzzerEnabler();

    File getOutputDir();

    CleanupTasks getCleanupTasks();
}
