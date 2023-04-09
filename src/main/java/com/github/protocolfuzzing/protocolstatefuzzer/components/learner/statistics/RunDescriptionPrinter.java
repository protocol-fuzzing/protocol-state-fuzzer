package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import java.io.PrintWriter;

public interface RunDescriptionPrinter {
    default void printRunDescription(PrintWriter printWriter) {
        printWriter.println();
        printRunDescriptionSelf(printWriter);
        printRunDescriptionRec(printWriter);
    }

    // print Run Description for the current instance
    public void printRunDescriptionSelf(PrintWriter printWriter);

    // print Run Description recursively for all the appropriate inner instances
    default void printRunDescriptionRec(PrintWriter printWriter) {
    }
}
