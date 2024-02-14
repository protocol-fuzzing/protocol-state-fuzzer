package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.learning.ralambda.RaLambda;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import net.automatalib.alphabet.Alphabet;

public class RALearner<I,O> implements PSFLearner<I, O, DefaultQuery<PSymbolInstance, Boolean>> {
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
    public RAStateMachine<I,O> getHypothesis() {
        RegisterAutomaton hyp = this.learningAlgorithm.getHypothesis();
        return new RAStateMachine<I,O>(hyp, this.alphabet);
    }

    /**
     * @param ce a counter example for the current hypothesis
     */
    @Override
    public void refineHypothesis(DefaultQuery<PSymbolInstance, Boolean> ce) {
        this.learningAlgorithm.addCounterexample(ce);
    }
}
