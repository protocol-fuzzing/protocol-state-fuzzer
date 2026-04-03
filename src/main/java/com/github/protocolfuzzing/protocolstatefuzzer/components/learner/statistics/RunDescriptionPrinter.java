package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for enabling the printing of a run description useful for the configuration classes.
 * <p>
 * The run description is split into two parts:
 * <ul>
 * <li>self, which refers to the run configuration of the implementing class
 * <li>rec (recursive), which refers to the run configuration of the inner classes
 * of the implementing class that also implement this interface
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
     * @param printWriter the PrintWriter to be used
     */
    default void printRunDescription(PrintWriter printWriter) {
        printWriter.println();
        printRunDescriptionSelf(printWriter);
        printRunDescriptionRec(printWriter);
    }

    /**
     * Prints the run description of the implementing class only.
     *
     * @param printWriter the PrintWriter to be used
     */
    void printRunDescriptionSelf(PrintWriter printWriter);

    /**
     * Prints the run description of the inner classes only of the implementing class.
     *
     * @param printWriter the PrintWriter to be used
     */
    default void printRunDescriptionRec(PrintWriter printWriter) {
        // do nothing
    }

    /**
     * Prints the run description of the provided parameter.
     * <p>
     * This targets generic parameters.
     * </p>
     *
     * @param printWriter the PrintWriter to be used
     * @param name        the parameter name
     * @param value       the parameter value
     */
    default void printRDParam(PrintWriter printWriter, String name, Object value) {
        if (value == null) {
            printWriter.println("# " + name);
        } else {
            printWriter.println(name + "\n" + value);
        }
    }

    /**
     * Prints the run description of the provided list parameter.
     * <p>
     * This targets list parameters that require comma-separation without brackets.
     * </p>
     *
     * @param <T>         the element type
     * @param printWriter the PrintWriter to be used
     * @param name        the parameter name
     * @param values      the parameter values
     */
    default <T> void printRDListParam(PrintWriter printWriter, String name, List<T> values) {
        if (values == null || values.isEmpty()) {
            printWriter.println("# " + name);
        } else {
            printWriter.println(name + "\n" + values.stream().map(T::toString).collect(Collectors.joining(",")));
        }
    }

    /**
     * Prints the run description of the provided String parameter.
     * <p>
     * This targets String parameters that can be empty (or null).
     * </p>
     *
     * @param printWriter the PrintWriter to be used
     * @param name        the parameter name
     * @param value       the parameter value
     */
    default void printRDStringParam(PrintWriter printWriter, String name, String value) {
        if (value == null) {
            printWriter.println("# " + name);
        } else if (value.isEmpty()) {
            // this can be user provided, so we output it back
            printWriter.println(name + "\n" + "\"\"");
        } else {
            printWriter.println(name + "\n" + value);
        }
    }

    /**
     * Prints the run description of the provided boolean parameter.
     * <p>
     * This targets boolean parameters that take no argument and thus they can only
     * be false by default.
     * </p>
     *
     * @param printWriter the PrintWriter to be used
     * @param name        the parameter name
     * @param value       the parameter value
     */
    default void printRDBooleanParam(PrintWriter printWriter, String name, Boolean value) {
        if (value == null || value == false) {
            printWriter.println("# " + name);
        } else {
            printWriter.println(name);
        }
    }
}
