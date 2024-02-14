package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class PSFMealyLearner<I, O> implements PSFLearner<I, O, DefaultQuery<I, Word<O>>> {
    private LearningAlgorithm.MealyLearner<I, O> learningAlgorithm;
    private Alphabet<I> alphabet;

    public PSFMealyLearner(LearningAlgorithm.MealyLearner<I, O> learner, Alphabet<I> alphabet) {
        this.learningAlgorithm = learner;
        this.alphabet = alphabet;
    }

    @Override
    public void startLearning() {
        this.learningAlgorithm.startLearning();
    }

    @Override
    public StateMachine<I, O> getHypothesis() {
        MealyMachine<?, I, ?, O> hyp = this.learningAlgorithm.getHypothesisModel();
        return new StateMachine<>(hyp, alphabet);
    }

    /**
     * @param ce a counter example for the current hypothesis
     */
    @Override
    public void refineHypothesis(DefaultQuery<I, Word<O>> ce) {
        this.learningAlgorithm.refineHypothesis(ce);
    }
}
