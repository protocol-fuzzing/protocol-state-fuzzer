package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import net.automatalib.alphabet.Alphabet;

import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.learnlib.ralib.automata.util.RAToDot;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class RAStateMachine extends AbstractStateMachine {

    protected RegisterAutomaton automata;
    protected Alphabet<ParameterizedSymbol> alphabet;

    public RAStateMachine(RegisterAutomaton automata, Alphabet<ParameterizedSymbol> alphabet) {
        this.automata = automata;
        this.alphabet = alphabet;
    }

    public Alphabet<ParameterizedSymbol> getAlphabet() {
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

    public RAStateMachine copy() {
        // FIXME: Figure out a way to copy a register automaton
        RegisterAutomaton newAutomaton;

        return new RAStateMachine(automata, alphabet);
    }

    @Override
    public String toString() {
        return this.automata.toString();
    }
}
