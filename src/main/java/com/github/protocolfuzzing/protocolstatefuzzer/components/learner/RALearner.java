package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.learning.ralambda.RaLambda;
import de.learnlib.ralib.equivalence.IORandomWalk;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.learnlib.ralib.automata.RegisterAutomaton;
import net.automatalib.alphabet.Alphabet;

public class RALearner implements PSFLearner<DefaultQuery<ParameterizedSymbol, Boolean>> {
    private RaLambda learningAlgorithm;
    private Alphabet<ParameterizedSymbol> alphabet;

    public RALearner(RaLambda learner, Alphabet<ParameterizedSymbol> alphabet) {
        this.learningAlgorithm = learner;
        this.alphabet = alphabet;
    }

    @Override
    public void startLearning() {
        this.learningAlgorithm.learn();
    }

    @Override
    public RAStateMachine getHypothesis() {
        RegisterAutomaton hyp = this.learningAlgorithm.getHypothesis();
        return new RAStateMachine(hyp, this.alphabet);
    }

    /**
     * @param ce a counter example for the current hypothesis
     */
    @Override
    public void refineHypothesis(DefaultQuery<ParameterizedSymbol, Boolean> ce) {
        this.learningAlgorithm.addCounterExample(ce);
    }
}