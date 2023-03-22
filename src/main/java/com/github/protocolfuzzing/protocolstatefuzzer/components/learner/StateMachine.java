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

public class StateMachine {
    private static final Logger LOGGER = LogManager.getLogger();

    protected MealyMachine<?, AbstractInput, ?, AbstractOutput> mealyMachine;

    protected Alphabet<AbstractInput> alphabet;

    public StateMachine(MealyMachine<?, AbstractInput, ?, AbstractOutput> mealyMachine, Alphabet<AbstractInput> alphabet) {
        this.mealyMachine = mealyMachine;
        this.alphabet = alphabet;
    }

    public MealyMachine<?, AbstractInput, ?, AbstractOutput> getMealyMachine() {
        return mealyMachine;
    }

    public void setMealyMachine(MealyMachine<?, AbstractInput, ?, AbstractOutput> mealyMachine) {
        this.mealyMachine = mealyMachine;
    }

    public Alphabet<AbstractInput> getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(Alphabet<AbstractInput> alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * Exports the hypothesis to the supplied file and generates a corresponding
     * viewable .pdf model.
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

    public void export(Writer writer) {
        try {
            GraphDOT.write(mealyMachine, alphabet, writer);
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("Could not export model to dot file");
        }
    }

    /**
     * Creates a low level copy of the state machine.
     */
    public StateMachine copy() {
        CompactMealy<AbstractInput, AbstractOutput> mealyCopy = new CompactMealy<>(alphabet);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, mealyMachine, alphabet, mealyCopy);
        return new StateMachine(mealyCopy, new ListAlphabet<>(new ArrayList<>(alphabet)));
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        export(sw);
        return sw.toString();
    }
}
