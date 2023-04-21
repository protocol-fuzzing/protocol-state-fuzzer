package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

/**
 * Represents a Mealy Machine and its input alphabet.
 */
public class StateMachine {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected MealyMachine<?, AbstractInput, ?, AbstractOutput> mealyMachine;

    /** Stores the constructor parameter. */
    protected Alphabet<AbstractInput> alphabet;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param mealyMachine  the Mealy Machine to be used
     * @param alphabet      the input alphabet of the Mealy Machine
     */
    public StateMachine(MealyMachine<?, AbstractInput, ?, AbstractOutput> mealyMachine, Alphabet<AbstractInput> alphabet) {
        this.mealyMachine = mealyMachine;
        this.alphabet = alphabet;
    }

    /**
     * Returns the stored value of {@link #mealyMachine}.
     *
     * @return  the stored value of {@link #mealyMachine}
     */
    public MealyMachine<?, AbstractInput, ?, AbstractOutput> getMealyMachine() {
        return mealyMachine;
    }

    /**
     * Returns the stored value of {@link #alphabet}.
     *
     * @return  the stored value of {@link #alphabet}
     */
    public Alphabet<AbstractInput> getAlphabet() {
        return alphabet;
    }

    /**
     * Creates the destination file, to which the hypothesis is exported and provides
     * the option to also generate a PDF file if the dot utility is found in the system.
     *
     * @param graphFile    the destination file that is created
     * @param generatePdf  <code>true</code> if also a PDF file should be generated 
     *                     using the system's dot utility
     */
    public void export(File graphFile, boolean generatePdf) {
        try {
            graphFile.createNewFile();
            export(new FileWriter(graphFile));
        } catch (IOException e) {
            LOGGER.warn("Could not create file {}", graphFile.getAbsolutePath());
        }

        if (generatePdf) {
            String dotFilename = graphFile.getAbsolutePath();
            String pdfFilename = dotFilename.endsWith(".dot") ? dotFilename.replace(".dot", ".pdf") :
                    dotFilename + ".pdf";
            String[] cmdArray = new String[]{"dot", "-Tpdf", dotFilename, "-o", pdfFilename};
            try {
                Runtime.getRuntime().exec(cmdArray);
            } catch (IOException e) {
                LOGGER.warn("Could not export model to pdf");
            }
        }

    }

    /**
     * Exports the StateMachine using the specified Writer, which is closed after 
     * the successful export.
     *
     * @param writer  the Writer to be used for the export
     */
    protected void export(Writer writer) {
        try {
            GraphDOT.write(mealyMachine, alphabet, writer);
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("Could not export model to dot file");
        }
    }

    /**
     * Creates and returns a low level copy of the state machine.
     *
     * @return  the low level copy of the state machine
     */
    public StateMachine copy() {
        CompactMealy<AbstractInput, AbstractOutput> mealyCopy = new CompactMealy<>(alphabet);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, mealyMachine, alphabet, mealyCopy);
        return new StateMachine(mealyCopy, new ListAlphabet<>(new ArrayList<>(alphabet)));
    }

    /**
     * Overrides the default method.
     *
     * @return  the string representation of the StateMachine
     */
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        export(sw);
        return sw.toString();
    }
}
