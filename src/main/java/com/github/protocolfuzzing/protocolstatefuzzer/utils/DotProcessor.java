package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Class for processing DOT files.
 */
public class DotProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Exports the provided DOT file to PDF using the {@code dot} utility in
     * the system's PATH.
     * <p>
     * If the {@code dot} utility is not present in the system's PATH, then
     * a warning is logged.
     *
     * @param dotInput  the DOT input file to be exported to PDF
     */
    public static void exportToPDF(File dotInput) {
        String dotFilename = dotInput.getAbsolutePath();
        String pdfFilename = dotFilename.endsWith(".dot") ? dotFilename.replace(".dot", ".pdf") : dotFilename + ".pdf";

        try {
            String[] cmdArray = new String[]{"dot", "-Tpdf", dotFilename, "-o", pdfFilename};
            Runtime.getRuntime().exec(cmdArray);
        } catch (IOException e) {
            LOGGER.warn("Could not export {} to {}: {}", dotFilename, pdfFilename, e.getMessage());
        }
    }
}
