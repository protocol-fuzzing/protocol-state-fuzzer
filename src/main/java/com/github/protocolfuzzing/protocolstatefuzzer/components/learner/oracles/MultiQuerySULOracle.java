package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles;

import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Extension of SULOracle able to return a reference to the underlying
 * DataWordSUL and query the SUL multiple times to get the majority output.
 */
public class MultiQuerySULOracle extends SULOracle {

    private Logger LOGGER = LogManager.getLogger();
    /** Stores the underlying DataWordSUL */
    protected DataWordSUL sul;

    /** Minimum multiplier for {@link #runs} used in probabilistic sanitization. */
    protected static final int PROBABILISTIC_MIN_MULTIPLIER = 2;

    /** Maximum multiplier for {@link #runs} used in probabilistic sanitization. */
    protected static final int PROBABILISTIC_MAX_MULTIPLIER = 7;

    /** Acceptable threshold percentage for an answer after multiple runs. */
    protected static final double ACCEPTABLE_PROBABILISTIC_THRESHOLD = 0.8;

    /**
     * Passable threshold percentage (less than acceptable) for an answer after
     * multiple runs.
     */
    protected static final double PASSABLE_PROBABILISTIC_THRESHOLD = 0.4;

    /** Stores the constructor parameter. */
    protected int runs;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul   the underlying DataWordSUL
     * @param error the error symbol to be used
     * @param runs  the number of times to run the queries
     */
    public MultiQuerySULOracle(DataWordSUL sul, ParameterizedSymbol error, Integer runs) {
        super(sul, error);
        this.sul = sul;
        this.runs = runs;
    }

    /**
     * Returns the underlying DataWordSUL
     *
     * @return the underlying DataWordSUL
     */
    public DataWordSUL getDataWordSUL() {
        return sul;
    }

    @Override
    public Word<PSymbolInstance> trace(Word<PSymbolInstance> input) {
        Map<Word<PSymbolInstance>, Integer> wordOccurrenceMap = new LinkedHashMap<Word<PSymbolInstance>, Integer>();

        for (int tries = 0; tries < runs * PROBABILISTIC_MAX_MULTIPLIER; tries++) {
            Word<PSymbolInstance> output = super.trace(input);

            Integer occurrence = wordOccurrenceMap.getOrDefault(output, 0);
            wordOccurrenceMap.put(output, occurrence + 1);

            if (enoughTestsRun(tries)) {

                Entry<Word<PSymbolInstance>, Integer> mostCommonEntry = wordOccurrenceMap
                        .entrySet()
                        .stream()
                        .max(Entry.comparingByValue())
                        .orElseThrow();

                double likelihood = (double) mostCommonEntry.getValue() / (tries + 1);
                LOGGER.info("Most likely answer {} has likelihood {} after {} tests", mostCommonEntry.getKey(),
                        likelihood, tries);

                if (likelihood >= ACCEPTABLE_PROBABILISTIC_THRESHOLD) {
                    LOGGER.info("Likelihood {} greater or equal to {}, returning answer {}",
                            likelihood,
                            ACCEPTABLE_PROBABILISTIC_THRESHOLD,
                            mostCommonEntry.getKey());
                    return mostCommonEntry.getKey();
                } else if (likelihood >= PASSABLE_PROBABILISTIC_THRESHOLD) {
                    LOGGER.info("Likelihood {} greater or equal to {}, continuing execution",
                            likelihood,
                            PASSABLE_PROBABILISTIC_THRESHOLD);
                    continue;
                } else {
                    LOGGER.error("Likelihood {} below passable threshold {}",
                            likelihood,
                            PASSABLE_PROBABILISTIC_THRESHOLD);
                    Iterator<Word<PSymbolInstance>> outputIter = wordOccurrenceMap.keySet().iterator();
                    throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
                }
            }
        }
        LOGGER.error("Exhausted {} or more tests without having found an acceptable answer",
                runs * PROBABILISTIC_MAX_MULTIPLIER);
        Iterator<Word<PSymbolInstance>> outputIter = wordOccurrenceMap.keySet().iterator();
        throw new NonDeterminismException(input, outputIter.next(), outputIter.next()).makeCompact();
    }

    private boolean enoughTestsRun(int numberOfTests) {
        return numberOfTests >= runs * PROBABILISTIC_MIN_MULTIPLIER;
    }

}
