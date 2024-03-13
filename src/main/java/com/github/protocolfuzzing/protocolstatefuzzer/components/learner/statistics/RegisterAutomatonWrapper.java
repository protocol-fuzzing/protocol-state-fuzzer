package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.util.RAToDot;
import de.learnlib.ralib.words.PSymbolInstance;
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
 * @param <I> the type of input symbol
 */
public class RegisterAutomatonWrapper<I extends PSymbolInstance> implements StateMachineWrapper<Word<I>, Boolean> {

    /**
     * TODO: Missing docs
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected RegisterAutomaton automata;

    /** Stores the constructor parameter */
    protected Alphabet<I> alphabet;

    /**
     * Constructs a new instance from the fiven parameters.
     *
     * @param automata the Register Automata to be used.
     * @param alphabet the input alphabet of the Register Automata.
     */
    public RegisterAutomatonWrapper(RegisterAutomaton automata, Alphabet<I> alphabet) {
        this.automata = automata;
        this.alphabet = alphabet;
    }

    /**
     * Returns the input alphabet of the wrapped Register Automata.
     * @return the alphabet of the wrapped RA
     */
    public Alphabet<I> getAlphabet() {
        return this.alphabet;
    }

    /**
     * Creates the destination file and exports the wrapped Register Automata
     * in dot format.
     *
     * @param graphFile the destination file that is created
     */
    @Override
    public void export(File graphFile) {
        // TODO: should we just set this to false?
        Boolean acceptingOnly = false;
        String dotString = new RAToDot(this.automata, acceptingOnly).toString();

        try (FileWriter fWriter = new FileWriter(graphFile, StandardCharsets.UTF_8)) {
            fWriter.write(dotString);
        } catch (IOException e) {
            LOGGER.warn("Could not export model to file: {}", graphFile.getAbsolutePath());
        }
    }

    /**
     * Get the register automaton stored in link{#automata}.
     * @return the wrapped RA
     */
    public RegisterAutomaton getRegisterAutomaton() {
        return this.automata;
    }

    @Override
    public RegisterAutomatonWrapper<I> copy() {
        // FIXME: Figure out a way to copy a register automaton

        return new RegisterAutomatonWrapper<I>(automata, alphabet);
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
    public Boolean computeOutput(Word<I> input) {
        // TODO Auto-generated method stub
        return false;
    }

}
