package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config;

import com.beust.jcommander.Parameter;

/**
 * The standard TestRunner configuration.
 */
public class IdentifierConfigStandard implements IdentifierConfig {

    /**
     * Stores the JCommander Parameter -identify.
     * <p>
     * This is the option that enables identifying and should should point
     * to a DOT model containing an ADG
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-identify", description = "This is the option that enables identifying and "
        + "should point to a DOT model containing an ADG.")
    protected String adgPath = null;

    /**
     * Stores the JCommander Parameter -alphabet.
     * <p>
     * A file defining a superset of the alphabet of the Adg.
     * If it is not provided then the learning alphabet will be used
     * The alphabet is used to interpret inputs present in the Adg file
     * Each input in the alphabet has a name under which it appears in the specification.
     * In XML format, for example, the name is specified using the 'name' attribute.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-adg_alphabet", description = "A file defining a superset of the alphabet of the Adg. "
        + "If it is not provided then the learning alphabet will be used "
        + "The alphabet is used to interpret inputs present in the Adg file"
        + "Each input in the alphabet has a name under which it appears in the specification. "
        + "In XML format, for example, the name is specified using the 'name' attribute.")
    protected String adgAlphabetFilename = null;

    /**
     * Stores the JCommander Parameter -conformance.
     * <p>
     * Show the sequence of transitions at the end in a nicer form.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-conformance", description = "Do a conformance test if an initial match "
        + "is found through identification. It should point to a folder with DOT models")
    protected String getConformanceTest = null;

    /**
     * Constructor
     */
    public IdentifierConfigStandard() {}

    /**
     * Returns the value of {@link #adgPath}.
     *
     * @return the value of {@link #adgPath}
     */
    @Override
    public String getAdgPath() {
        return adgPath;
    }

    /**
     * Returns the value of {@link adgAlphabetFilename}
     *
     * @return the value of {@link adgAlphabetFilename}
     */
    @Override
    public String getAlphabetFilename() {
        return adgAlphabetFilename;
    }

    /**
     * Returns the value of {@link #getConformanceTest}.
     *
     * @return the value of {@link #getConformanceTest}
     */
    @Override
    public String getConformance() {
        return getConformanceTest;
    }
}
