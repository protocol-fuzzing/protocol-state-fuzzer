package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.query.DefaultQuery;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.learnlib.algorithm.LearningAlgorithm;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

public class PSFMealyLearner implements PSFLearner<DefaultQuery<AbstractInput, Word<AbstractOutput>>> {
    private LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> learningAlgorithm;
    private Alphabet<AbstractInput> alphabet;

    public PSFMealyLearner(LearningAlgorithm.MealyLearner<AbstractInput, AbstractOutput> learner,
            Alphabet<AbstractInput> alphabet) {
        this.learningAlgorithm = learner;
        this.alphabet = alphabet;
    }

    @Override
    public void startLearning() {
        this.learningAlgorithm.startLearning();
    }

    @Override
    public StateMachine getHypothesis() {
        MealyMachine hyp = this.learningAlgorithm.getHypothesisModel();
        return new StateMachine(hyp, alphabet);
    }

    /**
     * @param ce a counter example for the current hypothesis
     */
    @Override
    public void refineHypothesis(DefaultQuery<AbstractInput, Word<AbstractOutput>> ce) {
        this.learningAlgorithm.refineHypothesis(ce);
    }
}