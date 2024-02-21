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

public class RegisterAutomatonWrapper<I extends PSymbolInstance> implements StateMachineWrapper<Word<I>, Boolean> {
    private static final Logger LOGGER = LogManager.getLogger();

    protected RegisterAutomaton automata;
    protected Alphabet<I> alphabet;

    public RegisterAutomatonWrapper(RegisterAutomaton automata, Alphabet<I> alphabet) {
        this.automata = automata;
        this.alphabet = alphabet;
    }

    public Alphabet<I> getAlphabet() {
        return this.alphabet;
    }

    @Override
    public void export(File graphFile) {
        // TODO: should we just set this to false?
        Boolean acceptingOnly = false;
        String dotString = new RAToDot(this.automata, acceptingOnly).toString();

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
        return null;
    }

}
