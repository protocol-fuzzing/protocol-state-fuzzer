package io.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.List;

/**
 * Chains together equivalence oracles for register automata.
 */
public class RAEQOracleChain extends EQOracleChain<RegisterAutomaton, PSymbolInstance, Boolean>
    implements IOEquivalenceOracle {

    /**
     * Constructs an instance for the given oracles.
     *
     * @param oracles equivalence oracles for register automata
     */
    public RAEQOracleChain(
        List<? extends EquivalenceOracle<? super RegisterAutomaton, PSymbolInstance, Boolean>> oracles) {
        super(oracles);
    }

}
