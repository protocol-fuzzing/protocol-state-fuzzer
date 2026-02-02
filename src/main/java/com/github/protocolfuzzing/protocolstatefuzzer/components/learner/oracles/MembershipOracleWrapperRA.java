package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

import java.util.Collection;

/**
 * A wrapper that allows the RALib SULOracle to be used where
 * a MembershipOracle is required, since SULOracle does not
 * implement this interface.
 */
public class MembershipOracleWrapperRA implements MembershipOracle<PSymbolInstance, Word<PSymbolInstance>> {

    /** The wrapped oracle */
    private SULOracle wrappedOracle;

    /**
     * Constructs a wrapper around the provided instance
     * @param wrappedOracle    The SULOracle to wrap
     */
    public MembershipOracleWrapperRA(SULOracle wrappedOracle) {
        this.wrappedOracle = wrappedOracle;
    }

    @Override
    public Word<PSymbolInstance> answerQuery(Word<PSymbolInstance> inputWord) {
        PSymbolInstance placeholderElement = new PSymbolInstance(new OutputSymbol("PLACEHOLDER ELEMENT"));
        WordBuilder<PSymbolInstance> ioTraceBuilder = new WordBuilder<PSymbolInstance>();
        for (PSymbolInstance i: inputWord) {
            ioTraceBuilder.add(i);
            ioTraceBuilder.add(placeholderElement);
        }

        Word<PSymbolInstance> ioTrace = wrappedOracle.trace(ioTraceBuilder.toWord());
        WordBuilder<PSymbolInstance> outputBuilder = new WordBuilder<PSymbolInstance>();
        for (PSymbolInstance symInstance : ioTrace) {
            if (symInstance.getBaseSymbol() instanceof OutputSymbol) {
                outputBuilder.add(symInstance);
            }
        }

        return outputBuilder.toWord();
    }

    @Override
    public void processQueries(Collection<? extends Query<PSymbolInstance, Word<PSymbolInstance>>> queries) {
        for (Query<PSymbolInstance, Word<PSymbolInstance>> query : queries) {
            query.answer(answerQuery(query.getInput()));
        }
    }
}
