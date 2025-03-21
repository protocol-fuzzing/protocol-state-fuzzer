package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Wraps a Mealy Machine and its input alphabet.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class MealyMachineWrapper<I, O> implements StateMachineWrapper<Word<I>, Word<O>> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected MealyMachine<?, I, ?, O> mealyMachine;

    /** Stores the constructor parameter. */
    protected Alphabet<I> alphabet;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param mealyMachine  the Mealy Machine to be used
     * @param alphabet      the input alphabet of the Mealy Machine
     */
    public MealyMachineWrapper(MealyMachine<?, I, ?, O> mealyMachine, Alphabet<I> alphabet) {
        this.mealyMachine = mealyMachine;
        this.alphabet = alphabet;
    }

    /**
     * Returns the stored {@link #mealyMachine}.
     *
     * @return  the stored {@link #mealyMachine}
     */
    public MealyMachine<?, I, ?, O> getMealyMachine() {
        return mealyMachine;
    }

    /**
     * Creates the destination file, to which the hypothesis is exported and provides
     * the option to also generate a PDF file if the dot utility is found in the system.
     *
     * @param graphFile    the destination file that is created
     */
    @Override
    public void export(File graphFile) {
        try (FileWriter fWriter = new FileWriter(graphFile, StandardCharsets.UTF_8)) {
            GraphDOT.write(mealyMachine, alphabet, fWriter);
        } catch (IOException e) {
            LOGGER.warn("Could not export model to file: {}", graphFile.getAbsolutePath());
        }
    }

    /**
     * Creates and returns a low level copy of the state machine.
     *
     * @return  the low level copy of the state machine
     */
    @Override
    public MealyMachineWrapper<I, O> copy() {
        CompactMealy<I, O> mealyCopy = new CompactMealy<>(alphabet);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, mealyMachine, alphabet, mealyCopy);
        return new MealyMachineWrapper<>(mealyCopy, new ListAlphabet<>(new ArrayList<>(alphabet)));
    }

    @Override
    public int getMachineSize() {
        return mealyMachine.size();
    }

    @Override
    public Word<O> computeOutput(Word<I> input) {
        return mealyMachine.computeOutput(input);
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of this instance
     */
    @Override
    public String toString() {
        try (StringWriter sWriter = new StringWriter()) {
            GraphDOT.write(mealyMachine, alphabet, sWriter);
            return sWriter.toString();
        } catch (IOException e) {
            LOGGER.warn("Could not convert model to string");
            return "";
        }
    }
}
