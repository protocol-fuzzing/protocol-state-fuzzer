package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * The configuration regarding the Mapper.
 * <p>
 * It can be extended with JCommander Parameters regarding the protocol-specific Mapper.
 */
public class MapperConfig implements RunDescriptionPrinter {

    /** The default filename in resources that configures the connection between Mapper and SUL. */
    public static final String DEFAULT_MAPPER_CONNECTION_CONFIG = "default_mapper_connection.config";

    /**
     * Stores the JCommander Parameter -mapperConnectionConfig.
     * <p>
     * Configuration file for the connection of Mapper with the running SUL process.
     * In case it is not provided, then the file defaults to {@link #DEFAULT_MAPPER_CONNECTION_CONFIG}.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-mapperConnectionConfig", description = "Configuration file for the connection of "
            + "Mapper with the running SUL process")
    protected String mapperConnectionConfig = null;

    /**
     * Stores the JCommander Parameter -repeatingOutputs.
     * <p>
     * Single or repeated occurrences of these outputs are mapped to a single
     * repeating output with the {@link AbstractOutput#REPEATING_INDICATOR} appended.
     * Used for outputs that the SUL may repeat an arbitrary number of times
     * which may cause non-determinism.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-repeatingOutputs", description = "Single or repeated occurrences of these "
            + "outputs are mapped to a single repeating output with the " + AbstractOutput.REPEATING_INDICATOR
            + " appended. Used for outputs that the SUL may repeat an arbitrary number "
            + "of times which may cause non-determinism")
    protected List<String> repeatingOutputs = null;

    /**
     * Stores the JCommander Parameter -socketClosedAsTimeout.
     * <p>
     * Uses {@link AbstractOutput#TIMEOUT} instead of {@link AbstractOutput#SOCKET_CLOSED}
     * outputs to identify when the SUL process has terminated.
     * Useful for preventing non-determinism due to the arbitrary waiting duration
     * caused by the non-responding SUL while its process eventually terminates.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-socketClosedAsTimeout", description = "Uses "
            + AbstractOutput.TIMEOUT + " instead of " + AbstractOutput.SOCKET_CLOSED
            + " outputs to identify when the system process has terminated. "
            + "Useful for preventing non-determinism due to the arbitrary waiting duration "
            + "caused by the non-responding SUL while its process eventually terminates")
    protected boolean socketClosedAsTimeout = false;

    /**
     * Stores the JCommander Parameter -disabledAsTimeout.
     * <p>
     * Uses {@link AbstractOutput#TIMEOUT} instead of {@link AbstractOutput#DISABLED}.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-disabledAsTimeout", description = "Uses " + AbstractOutput.TIMEOUT + " instead of "
            + AbstractOutput.DISABLED)
    protected boolean disabledAsTimeout = false;

    /**
     * Stores the JCommander Parameter -dontMergeRepeating.
     * <p>
     * Disables merging of repeated outputs. By default the mapper merges outputs
     * which are repeated in immediate succession into a single output with
     * {@link AbstractOutput#REPEATING_INDICATOR} appended.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-dontMergeRepeating", description = "Disables merging of repeated outputs. "
            + "By default the mapper merges outputs which are repeated in immediate succession  "
            + "into a single output with '" + AbstractOutput.REPEATING_INDICATOR + "' appended")
    protected boolean dontMergeRepeating = false;

    /**
     * Returns the stored value of {@link #mapperConnectionConfig}.
     *
     * @return  the stored value of {@link #mapperConnectionConfig}
     */
    public String getMapperConnectionConfig() {
        return mapperConnectionConfig;
    }

    /**
     * Returns the input stream of {@link #mapperConnectionConfig} if it is not null
     * or the default input stream from resources of {@link #DEFAULT_MAPPER_CONNECTION_CONFIG}.
     *
     * @return  the input stream of {@link #mapperConnectionConfig} if it is not null
     *          or the default input stream from resources of {@link #DEFAULT_MAPPER_CONNECTION_CONFIG}
     *
     * @throws IOException  if an IO error occurs
     */
    public InputStream getMapperConnectionConfigInputStream() throws IOException {
        if (mapperConnectionConfig == null) {
            return getClass().getClassLoader().getResourceAsStream(DEFAULT_MAPPER_CONNECTION_CONFIG);
        }

        return new FileInputStream(mapperConnectionConfig);
    }

    /**
     * Returns the stored value of {@link #repeatingOutputs}.
     *
     * @return  the stored value of {@link #repeatingOutputs}
     */
    public List<String> getRepeatingOutputs() {
        return repeatingOutputs;
    }

    /**
     * Returns the stored value of {@link #socketClosedAsTimeout}.
     *
     * @return  the stored value of {@link #socketClosedAsTimeout}
     */
    public boolean isSocketClosedAsTimeout() {
        return socketClosedAsTimeout;
    }

    /**
     * Returns the stored value of {@link #disabledAsTimeout}.
     *
     * @return  the stored value of {@link #disabledAsTimeout}
     */
    public boolean isDisabledAsTimeout() {
        return disabledAsTimeout;
    }

    /**
     * Returns the opposite of the stored value of {@link #dontMergeRepeating}.
     *
     * @return  the opposite of the stored value of {@link #dontMergeRepeating}
     */
    public boolean isMergeRepeating() {
        return !dontMergeRepeating;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("MapperConfig Parameters");
        printWriter.println("Repeating Outputs: " + getRepeatingOutputs());
        printWriter.println("Socket Closed as Timeout: " + isSocketClosedAsTimeout());
        printWriter.println("Disabled as Timeout: " + isDisabledAsTimeout());
        printWriter.println("Merge Repeating: " + isMergeRepeating());
    }
}
