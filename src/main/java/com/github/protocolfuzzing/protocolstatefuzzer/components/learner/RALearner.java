package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.learning.ralambda.RaLambda;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

public class RALearner implements PSFLearner<PSymbolInstance, PSymbolInstance, Word<PSymbolInstance>, Boolean, DefaultQuery<PSymbolInstance, Boolean>, RegisterAutomatonWrapper> {
    private RaLambda learningAlgorithm;
    private Alphabet<PSymbolInstance> alphabet;

    public RALearner(RaLambda learner, Alphabet<PSymbolInstance> alphabet) {
        this.learningAlgorithm = learner;
        this.alphabet = alphabet;
    }

    @Override
    public void startLearning() {
        this.learningAlgorithm.learn();
    }

    @Override
    public RegisterAutomatonWrapper getHypothesis() {
        RegisterAutomaton hyp = this.learningAlgorithm.getHypothesis();
        return new RegisterAutomatonWrapper(hyp, this.alphabet);
    }

    /**
     * @param ce a counter example for the current hypothesis
     */
    @Override
    public void refineHypothesis(DefaultQuery<PSymbolInstance, Boolean> ce) {
        this.learningAlgorithm.addCounterexample(ce);
    }
}
