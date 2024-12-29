package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.util.RAToDot;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a register automaton and its input alphabet.
 *
 * @param <B> the type of base symbols
 * @param <D> the type domain of inputs and outputs
 */
public class RegisterAutomatonWrapper<B extends ParameterizedSymbol, D extends PSymbolInstance>
        implements StateMachineWrapper<Word<D>, Boolean> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected RegisterAutomaton automata;

    /** Stores the constructor parameter */
    protected Alphabet<B> alphabet;

    /**
     * Constructs a new instance from the fiven parameters.
     *
     * @param automata the Register Automata to be used.
     * @param alphabet the input alphabet of the Register Automata.
     */
    public RegisterAutomatonWrapper(RegisterAutomaton automata, Alphabet<B> alphabet) {
        this.automata = automata;
        this.alphabet = alphabet;
    }

    /**
     * Returns the input alphabet of the wrapped Register Automata.
     *
     * @return the alphabet of the wrapped RA
     */
    public Alphabet<B> getAlphabet() {
        return this.alphabet;
    }

    /**
     * Creates the destination file and exports the wrapped Register Automata
     * in dot format. With acceptingOnly set to false by default.
     *
     * @param graphFile the destination file that is created
     */
    @Override
    public void export(File graphFile) {
        Boolean acceptingOnly = false;
        this.export(graphFile, acceptingOnly);
    }

    /**
     * Creates the destination file and exports the wrapped Register Automata
     * in dot format.
     *
     * @param graphFile     the destination file that is created
     * @param acceptingOnly if true draw only accepting states
     */
    public void export(File graphFile, boolean acceptingOnly) {
        String dotString = new RAToDot(this.automata, acceptingOnly).toString();

        try (FileWriter fWriter = new FileWriter(graphFile, StandardCharsets.UTF_8)) {
            fWriter.write(dotString);
        } catch (IOException e) {
            LOGGER.warn("Could not export model to file: {}", graphFile.getAbsolutePath());
        }
    }

    /**
     * Get the register automaton stored in link{#automata}.
     *
     * @return the wrapped RA
     */
    public RegisterAutomaton getRegisterAutomaton() {
        return this.automata;
    }

    /**
     * Get a shallow copy of the RegisterAutomatonWrapper.
     * Note: Can be made deep once RALib implements copying of RegisterAutomata.
     *
     * @return a new wrapper with the same automata and alphabet.
     */
    @Override
    public RegisterAutomatonWrapper<B, D> copy() {
        return new RegisterAutomatonWrapper<B, D>(automata, alphabet);
    }

    @Override
    public String toString() {
        return this.automata.toString();
    }

    @Override
    public int getMachineSize() {
        return this.automata.size();
    }

    @Override
    public Boolean computeOutput(Word<D> input) {
        // FIXME: We never found the way this is supposed to be implemented.
        return false;
    }

}
