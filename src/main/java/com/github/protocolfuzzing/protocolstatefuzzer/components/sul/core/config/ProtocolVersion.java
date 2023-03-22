package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import java.util.LinkedHashMap;
import java.util.List;

public class ProtocolVersion {
    /*
     * static fields and methods
     */
    protected static final LinkedHashMap<String, ProtocolVersion> versionMap = new LinkedHashMap<>();

    // called once in static block of ToolConfig
    public static void fillMapOnce(String[] versions) {
        if (versionMap.isEmpty()) {
            for (String version : versions) {
                versionMap.putIfAbsent(version, new ProtocolVersion(version));
            }
        }
    }

    public static boolean contains(String version) {
        return versionMap.containsKey(version);
    }

    public static ProtocolVersion valueOf(String version) throws IllegalArgumentException {
        if (versionMap.containsKey(version)) {
            return versionMap.get(version);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static List<ProtocolVersion> values() {
        return versionMap.values().stream().toList();
    }

    public static List<String> names() {
        return versionMap.keySet().stream().toList();
    }

    /*
     * non-static fields and methods
     */
    protected final String name;
    protected ProtocolVersion(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean equals(String protocolVersion) {
        return name.equals(protocolVersion);
    }
}
