package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.word.Word;

import java.util.Collection;

/**
 * A wrapper that allows the RALib SULOracle to be used where
 * a MembershipOracle is required, since SULOracle does not
 * implement this interface.
 */
public class MembershipOracleWrapper implements MembershipOracle<PSymbolInstance, Word<PSymbolInstance>> {

    /** The wrapped oracle */
    private SULOracle wrappedOracle;

    /**
     * Constructs a wrapper around the provided instance
     * @param wrappedOracle    The SULOracle to wrap
     */
    public MembershipOracleWrapper(SULOracle wrappedOracle) {
        this.wrappedOracle = wrappedOracle;
    }

    @Override
    public Word<PSymbolInstance> answerQuery(Word<PSymbolInstance> inputWord) {
        return wrappedOracle.trace(inputWord);
    }

    @Override
    public void processQueries(Collection<? extends Query<PSymbolInstance, Word<PSymbolInstance>>> queries) {
        for (Query<PSymbolInstance, Word<PSymbolInstance>> query : queries) {
            query.answer(
                wrappedOracle.trace(query.getInput())
            );
        }
    }
}
