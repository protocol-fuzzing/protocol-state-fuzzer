package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierAdg.Node;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Identifies a model in a system under learning using an adaptive discriminating
 * graph (ADG) and a SUL instance.
 *
 * @param <M> the type of the model representation returned by the identifier
 */
public interface SulIdentifier<M> {

    /**
     * Traverses the ADG to identify the current model of the given SUL.
     * <p>
     * The method drives the SUL with inputs from the current ADG node,
     * interprets output labels, and advances through the graph until a leaf
     * node containing candidate models is reached.
     *
     * @param  identifierAdg the adaptive discriminating graph to traverse
     * @param  sul           the system under learning to query
     * @param  alphabet      the input alphabet used to map edge labels to input values
     * @param  <I>           input type
     * @param  <O>           output type
     *
     * @return               the leaf node reached after identification
     */
    static <I, O> Node identify(IdentifierAdg identifierAdg, SUL<I, O> sul, Alphabet<I> alphabet) {

        Logger LOGGER = LogManager.getLogger();

        Map<String, I> inputs = new LinkedHashMap<>();
        alphabet.forEach(i -> inputs.put(i.toString(), i));

        // Node currentNode = identifierAdg.getRoot();

        try {

            sul.pre(); // Initialize the SUL before starting the identification process

            String testInput;
            I inputWord;
            O outputWord;
            String outputString;
            Node check;

            while (!identifierAdg.getCurrentNode().isLeaf()) {
                testInput = identifierAdg.getNextInput(); // Get the next test input from the current node
                if (testInput.equals("reset")) {
                    resetSUL(sul);
                    identifierAdg.skipReset();
                    continue; // Skip the rest of the loop and start the next iteration
                }

                inputWord = inputs.get(testInput);
                if (inputWord == null)
                    throw new IllegalStateException("Input not found in alphabet provided for the ADG: " + testInput);
                outputWord = sul.step(inputWord);
                outputString = getOutputString(outputWord);

                check = identifierAdg.proceedToNextNodeWithOutput(outputString);
                if (check == null) {
                    check = identifierAdg.proceedToNextNodeWithOutput("other");
                    if (check == null) {
                        LOGGER.info("Output not found in ADG, returning empty node");
                        return new Node("");
                    }
                }

            }

        }
        finally {
            sul.post();
        }

        return identifierAdg.getCurrentNode();

    }

    private static <I, O> void resetSUL(SUL<I, O> sul) {
        sul.post();
        sul.pre();
    }

    /**
     * Converts the SUL output object to a string for ADG edge lookup.
     *
     * @param  outputWord output returned by the SUL
     * @param  <O>        output type
     *
     * @return            string representation of the output
     */
    private static <O> String getOutputString(O outputWord) {
        return outputWord.toString();
    }

    /**
     * Executes the identifier implementation.
     *
     * @return the set of model names identified, or an empty set if no match was found
     */
    public Set<String> run();

    /**
     * Used to run a conformance test. Creates a hypothesis from a given file
     *
     * @param  filePath the path to the folder containing the dot model and alphabet files
     *
     * @return          the corresponding LearnerResult, which can be empty if the test fails
     */
    public LearnerResult<M> conformanceTest(String filePath);

}
