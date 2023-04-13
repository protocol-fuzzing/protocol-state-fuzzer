package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import com.google.common.base.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Defines methods for exporting results that can have a main title, section
 * titles and subsection titles.
 */
public abstract class ExportableResult {

    /**
     * Delimiter that surrounds the section titles.
     */
    protected static String SECTION_TITLE_DELIM = Strings.repeat("-", 80);

    /**
     * Delimiter that surrounds the results' title.
     */
    protected static String TITLE_DELIM = Strings.repeat("=", 80);

    /**
     * Uses the {@link #doExport(PrintWriter)} method and closes the writer
     * afterwards.
     * <p>
     * A PrintWriter instance is used to wrap the writer parameter, in case the
     * writer parameter is not an instance of PrintWriter.
     *
     * @param writer  the writer to be used
     */
    public void export(Writer writer) {
        PrintWriter printWriter;

        if (writer instanceof PrintWriter) {
            printWriter = (PrintWriter) writer;
        } else {
            printWriter = new PrintWriter(writer);
        }

        doExport(printWriter);
        printWriter.close();
    }

    /**
     * Method to be overriden with the appropriate exporting behavior.
     *
     * @param printWriter  the printWriter to be used
     */
    protected abstract void doExport(PrintWriter printWriter);

    /**
     * Prints the title of the results.
     * <p>
     * Above and below the title is a line of {@link #TITLE_DELIM} and
     * an empty line.
     *
     * @param title        the results' title to be printed
     * @param printWriter  the printWriter to be used
     */
    protected void title(String title, PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(TITLE_DELIM);
        printWriter.println(title);
        printWriter.println(TITLE_DELIM);
        printWriter.println();
    }

    /**
     * Prints the title of a section.
     * <p>
     * Above and below the title is a line of {@link #SECTION_TITLE_DELIM} and
     * an empty line.
     *
     * @param title        the section title to be printed
     * @param printWriter  the printWriter to be used
     */
    protected void sectionTitle(String title, PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(SECTION_TITLE_DELIM);
        printWriter.println(title);
        printWriter.println(SECTION_TITLE_DELIM);
        printWriter.println();
    }

    /**
     * Prints the title of a subsection.
     * <p>
     * The title is surrounded by ==.
     * Above and below the title is an empty line.
     *
     * @param title        the subsection title to be printed
     * @param printWriter  the printWriter to be used
     */
    protected void subsectionTitle(String title, PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("== " + title + " ==");
        printWriter.println();
    }

    /**
     * Uses the {@link #doExport(PrintWriter)} and turns its output to
     * string.
     *
     * @return  the output of {@link #doExport(PrintWriter)}
     */
    public String exportToString() {
        StringWriter sw = new StringWriter();
        export(sw);
        return sw.toString();
    }
}
