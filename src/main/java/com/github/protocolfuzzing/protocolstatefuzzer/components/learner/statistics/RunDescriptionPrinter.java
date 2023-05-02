package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import java.io.PrintWriter;

/**
 * Interface for enabling the printing of a run description useful for the configuration classes.
 * <p>
 * The run description is split into two parts:
 * <ul>
 * <li> self, which refers to the run configuration of the implementing class
 * <li> rec (recursive), which refers to the run configuration of the inner classes
 *      of the implementing class that also implement this interface
 * </ul>
 * <p>
 * The default implementation of {@link #printRunDescription(PrintWriter)}
 * prints a new line and then uses {@link #printRunDescriptionSelf(PrintWriter)}
 * and {@link #printRunDescriptionRec(PrintWriter)}.
 * <p>
 * The default implementation of {@link #printRunDescriptionRec(PrintWriter)}
 * does nothing, assuming that there are no inner classes.
 */
public interface RunDescriptionPrinter {

    /**
     * Prints the run description of the implementing and inner classes.
     *
     * @param printWriter  the PrintWriter to be used
     */
    default void printRunDescription(PrintWriter printWriter) {
        printWriter.println();
        printRunDescriptionSelf(printWriter);
        printRunDescriptionRec(printWriter);
    }

    /**
     * Prints the run description of the implementing class only.
     *
     * @param printWriter  the PrintWriter to be used
     */
    public void printRunDescriptionSelf(PrintWriter printWriter);

    /**
     * Prints the run description of the inner classes only of the implementing class.
     *
     * @param printWriter  the PrintWriter to be used
     */
    default void printRunDescriptionRec(PrintWriter printWriter) {
    }
}
