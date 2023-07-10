package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config;

import java.io.PrintWriter;
import java.util.List;

/**
 * The empty Mapper configuration without any JCommander Parameters.
 */
public class MapperConfigEmpty implements MapperConfig {

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getMapperConnectionConfig() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public List<String> getRepeatingOutputs() {
        return null;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isSocketClosedAsTimeout() {
        return false;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isDisabledAsTimeout() {
        return false;
    }

    /**
     * Returns {@code true}.
     *
     * @return  {@code true}
     */
    @Override
    public boolean isMergeRepeating() {
        return true;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("MapperConfigEmpty Non-Explicit Parameters");
        printWriter.println("Mapper Connection Config: " + getMapperConnectionConfig());
        printWriter.println("Repeating Outputs: " + getRepeatingOutputs());
        printWriter.println("Socket Closed as Timeout: " + isSocketClosedAsTimeout());
        printWriter.println("Disabled as Timeout: " + isDisabledAsTimeout());
        printWriter.println("Merge Repeating: " + isMergeRepeating());
    }
}
