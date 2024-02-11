package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface for the timing probe process.
 */
public interface TimingProbe {
    /**
     * Returns a nice representation of a String to Integer map.
     *
     * @param map  the map to be represented
     * @return     the string representation
     */
    public static String present(Map<String, Integer> map) {
        return map.entrySet()
            .stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", ", "{ ", " }"));
    }

    /**
     * Runs the implemented timing probe.
     */
    public void run();
}
