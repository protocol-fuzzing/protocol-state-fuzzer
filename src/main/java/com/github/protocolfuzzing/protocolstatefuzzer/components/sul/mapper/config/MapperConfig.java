package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RunDescriptionPrinter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Interface regarding the configuration of the Mapper, which can be extended
 * with JCommander Parameters targeting the protocol-specific Mapper.
 */
public interface MapperConfig extends RunDescriptionPrinter {

    /** The default filename in resources that configures the connection between Mapper and SUL. */
    static final String DEFAULT_MAPPER_CONNECTION_CONFIG = "default_mapper_connection.config";

    /**
     * Returns null or the provided configuration file for the connection of Mapper
     * with the running SUL process.
     * <p>
     * Default value: null.
     *
     * @return  null or the provided configuration file
     */
    default String getMapperConnectionConfig() {
        return null;
    }

    /**
     * Returns the input stream of the provided configuration file if {@link #getMapperConnectionConfig}
     * is not null or the input stream of the {@link #DEFAULT_MAPPER_CONNECTION_CONFIG} file in resources,
     * which can be null if the file is missing.
     *
     * @return  the input stream of the provided configuration file if {@link #getMapperConnectionConfig}
     *          is not null or the input stream of the {@link #DEFAULT_MAPPER_CONNECTION_CONFIG} file in resources,
     *          which can be null if the file is missing
     *
     * @throws IOException  if an IO error occurs
     */
    default InputStream getMapperConnectionConfigInputStream() throws IOException {
        if (getMapperConnectionConfig() == null) {
            return getClass().getClassLoader().getResourceAsStream(DEFAULT_MAPPER_CONNECTION_CONFIG);
        }

        return new FileInputStream(getMapperConnectionConfig());
    }

    /**
     * Returns a list of repeating outputs or null.
     * <p>
     * Single or repeated occurrences of these outputs are mapped to a single
     * repeating output with the {@link MapperOutput#REPEATING_INDICATOR} appended.
     * Used for outputs that the SUL may repeat an arbitrary number of times
     * which may cause non-determinism.
     * <p>
     * Default value: null.
     *
     * @return  a list of repeating outputs or null if not provided
     */
    default List<String> getRepeatingOutputs() {
        return null;
    }

    /**
     * Indicates if {@link OutputBuilder#TIMEOUT} should be used instead
     * of {@link OutputBuilder#SOCKET_CLOSED} symbols to identify when the SUL
     * process has terminated.
     * <p>
     * Useful for preventing non-determinism due to the arbitrary waiting duration
     * caused by the non-responding SUL while its process eventually terminates.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the substitution of symbols should occur
     */
    default boolean isSocketClosedAsTimeout() {
        return false;
    }

    /**
     * Indicates if {@link OutputBuilder#TIMEOUT} should be used instead
     * of {@link OutputBuilder#DISABLED} symbols.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if the substitution of symbols should occur
     */
    default boolean isDisabledAsTimeout() {
        return false;
    }

    /**
     * Indicates if merging of repeated outputs should occur.
     * <p>
     * By default the mapper merges outputs which are repeated in immediate
     * succession into a single output with
     * {@link MapperOutput#REPEATING_INDICATOR} appended.
     * <p>
     * Default value: false.
     *
     * @return  {@code true} if merging of repeated outputs should occur
     */
    default boolean isMergeRepeating() {
        return false;
    }

    @Override
    default void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("MapperConfig Parameters");
        printWriter.println("Mapper Connection Config: " + getMapperConnectionConfig());
        printWriter.println("Repeating Outputs: " + getRepeatingOutputs());
        printWriter.println("Socket Closed as Timeout: " + isSocketClosedAsTimeout());
        printWriter.println("Disabled as Timeout: " + isDisabledAsTimeout());
        printWriter.println("Merge Repeating: " + isMergeRepeating());
    }
}
