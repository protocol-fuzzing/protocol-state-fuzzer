package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import java.util.Collection;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.word.Word;


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
	public void processQueries(Collection<? extends Query<PSymbolInstance, Word<PSymbolInstance>>> queries) {
		for (Query<PSymbolInstance, Word<PSymbolInstance>> query : queries) {
		    query.answer(
		        wrappedOracle.trace(query.getInput())
			);
		}
	}
}
