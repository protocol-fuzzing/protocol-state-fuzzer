package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config;

import com.beust.jcommander.Parameter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

import java.io.PrintWriter;
import java.util.List;


/**
 * The standard Mapper configuration.
 */
public class MapperConfigStandard implements MapperConfig {

    /**
     * Stores the JCommander Parameter -mapperConnectionConfig.
     * <p>
     * Configuration file for the connection of Mapper with the running SUL process.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-mapperConnectionConfig", description = "Configuration file for the connection of "
            + "Mapper with the running SUL process. Defaults to " + DEFAULT_MAPPER_CONNECTION_CONFIG
            + " file in resources")
    protected String mapperConnectionConfig = null;

    /**
     * Stores the JCommander Parameter -repeatingOutputs.
     * <p>
     * Single or repeated occurrences of these outputs are mapped to a single
     * repeating output with the {@link MapperOutput#REPEATING_INDICATOR} appended.
     * Used for outputs that the SUL may repeat an arbitrary number of times
     * which may cause non-determinism.
     * <p>
     * Default value: null.
     */
    @Parameter(names = "-repeatingOutputs", description = "Single or repeated occurrences of these "
            + "outputs are mapped to a single repeating output with the " + MapperOutput.REPEATING_INDICATOR
            + " appended. Used for outputs that the SUL may repeat an arbitrary number "
            + "of times which may cause non-determinism")
    protected List<String> repeatingOutputs = null;

    /**
     * Stores the JCommander Parameter -socketClosedAsTimeout.
     * <p>
     * Uses {@link OutputBuilder#TIMEOUT} instead of {@link OutputBuilder#SOCKET_CLOSED}
     * outputs to identify when the SUL process has terminated.
     * Useful for preventing non-determinism due to the arbitrary waiting duration
     * caused by the non-responding SUL while its process eventually terminates.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-socketClosedAsTimeout", description = "Uses "
            + OutputBuilder.TIMEOUT + " instead of " + OutputBuilder.SOCKET_CLOSED
            + " outputs to identify when the system process has terminated. "
            + "Useful for preventing non-determinism due to the arbitrary waiting duration "
            + "caused by the non-responding SUL while its process eventually terminates")
    protected boolean socketClosedAsTimeout = false;

    /**
     * Stores the JCommander Parameter -disabledAsTimeout.
     * <p>
     * Uses {@link OutputBuilder#TIMEOUT} instead of {@link OutputBuilder#DISABLED}.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-disabledAsTimeout", description = "Uses " + OutputBuilder.TIMEOUT + " instead of "
            + OutputBuilder.DISABLED)
    protected boolean disabledAsTimeout = false;

    /**
     * Stores the JCommander Parameter -dontMergeRepeating.
     * <p>
     * Disables merging of repeated outputs. By default the mapper merges outputs
     * which are repeated in immediate succession into a single output with
     * {@link MapperOutput#REPEATING_INDICATOR} appended.
     * <p>
     * Default value: false.
     */
    @Parameter(names = "-dontMergeRepeating", description = "Disables merging of repeated outputs. "
            + "By default the mapper merges outputs which are repeated in immediate succession  "
            + "into a single output with '" + MapperOutput.REPEATING_INDICATOR + "' appended")
    protected boolean dontMergeRepeating = false;

    @Override
    public String getMapperConnectionConfig() {
        return mapperConnectionConfig;
    }

    @Override
    public List<String> getRepeatingOutputs() {
        return repeatingOutputs;
    }

    @Override
    public boolean isSocketClosedAsTimeout() {
        return socketClosedAsTimeout;
    }

    @Override
    public boolean isDisabledAsTimeout() {
        return disabledAsTimeout;
    }

    @Override
    public boolean isMergeRepeating() {
        return !dontMergeRepeating;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("MapperConfigStandard Parameters");
        printWriter.println("Mapper Connection Config: " + getMapperConnectionConfig());
        printWriter.println("Repeating Outputs: " + getRepeatingOutputs());
        printWriter.println("Socket Closed as Timeout: " + isSocketClosedAsTimeout());
        printWriter.println("Disabled as Timeout: " + isDisabledAsTimeout());
        printWriter.println("Merge Repeating: " + isMergeRepeating());
    }
}
