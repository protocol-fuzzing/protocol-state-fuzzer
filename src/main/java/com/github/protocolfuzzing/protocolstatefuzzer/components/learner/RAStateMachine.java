package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.util.RAToDot;
import net.automatalib.alphabet.Alphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RAStateMachine<I, O> extends AbstractStateMachine<I, O> {

    private static final Logger LOGGER = LogManager.getLogger();

    protected RegisterAutomaton automata;
    protected Alphabet<I> alphabet;

    public RAStateMachine(RegisterAutomaton automata, Alphabet<I> alphabet) {
        this.automata = automata;
        this.alphabet = alphabet;
    }

    public Alphabet<I> getAlphabet() {
        return this.alphabet;
    }

    public void export(File graphFile) {
        Boolean acceptingOnly = false; // TODO: should we just set this to false?
        String dotString = (new RAToDot(this.automata, acceptingOnly)).toString();

        try (FileWriter fWriter = new FileWriter(graphFile, StandardCharsets.UTF_8)) {
            fWriter.write(dotString);
        } catch (IOException e) {
            // TODO: Why not log exception?
            LOGGER.warn("Could not export model to file: {}", graphFile.getAbsolutePath());
        }
    }

    public RegisterAutomaton getRegisterAutomaton() {
        return this.automata;
    }

    public RAStateMachine<I, O> copy() {
        // FIXME: Figure out a way to copy a register automaton
        RegisterAutomaton newAutomaton;

        return new RAStateMachine<I, O>(automata, alphabet);
    }

    @Override
    public String toString() {
        return this.automata.toString();
    }
}
