package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.DynamicParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyResolver {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FUZZER_PROPS = "fuzzer.properties";
    public static final String DEFAULT_FUZZER_PROPS = "default_fuzzer.properties";

    public static final String FUZZER_DIR = "fuzzer.dir";
    public static final String SUL_PORT = "sul.port";
    public static final String FUZZER_PORT = "fuzzer.port";

    // Stores default application properties as provided in the FUZZER_PROPS file
    protected static Map<String, String> defaultProps = new LinkedHashMap<>();

    // Stores application properties which include variable definitions
    @DynamicParameter(names = "-D", description = "Definitions for variables, which can be referred to in arguments by ${var}. "
            + "Variables are replaced with their corresponding values before the arguments are parsed."
            + "Can be passed as either JVM properties (after java) or as application properties.")
    protected static Map<String, String> dynamicProps = new LinkedHashMap<>();

    // Stores already loaded properties in order not to reload them on next invocation of loadProperties()
    protected static Map<String, Properties> propertiesCache = new LinkedHashMap<>();

    // cache userStrings in need of resolution, so as not to parse duplicates
    protected static Map<String, String> resolutionCache = new HashMap<>();


    // singleton fields and methods
    private static PropertyResolver instance = new PropertyResolver();

    private PropertyResolver() {
        if (instance != null) {
            throw new IllegalStateException("Instance has been created already");
        }
    }

    public static PropertyResolver getInstance() {
        return instance;
    }

    // load properties from filea and initialize defaultProps Map
    // to be invoked prior to argument parsing
    public static void initializeParsing() {

        Properties fuzzerProps = loadProperties();

        // system properties have priority over file defined ones
        for (String propName : fuzzerProps.stringPropertyNames()) {
            String systemPropValue = System.getProperty(propName);
            if (systemPropValue != null) {
                defaultProps.put(propName, systemPropValue);
            } else {
                defaultProps.put(propName, fuzzerProps.getProperty(propName));
            }
        }

        String fuzzerDir = System.getProperty(FUZZER_DIR);
        if (fuzzerDir == null) {
            fuzzerDir = System.getProperty("user.dir");
        }
        defaultProps.put(FUZZER_DIR, fuzzerDir);

        // SUL port: between 10000 and 39999
        String sulPort = fuzzerProps.getProperty(SUL_PORT);
        if (sulPort == null) {
            long sulSec = (System.currentTimeMillis() / 1000 % 30000) + 10000;
            sulPort = Long.toString(sulSec);
        }
        defaultProps.put(SUL_PORT, sulPort);

        // Fuzzer port: between 40000 and 65535 (= 0xFFFF or max port)
        String fuzzerPort = fuzzerProps.getProperty(FUZZER_PORT);
        if (fuzzerPort == null) {
            long fuzzSec = (System.currentTimeMillis() / 1000 % 25536) + 40000;
            fuzzerPort = Long.toString(fuzzSec);
        }
        defaultProps.put(FUZZER_PORT, fuzzerPort);
    }

    // clears the defaultProps and dynamicProps Maps
    // for the next parsing to have clean initial state
    // to be invoked after parsing
    public static void finalizeParsing() {
        defaultProps.clear();
        dynamicProps.clear();
    }

    // loads the properties from the appropriate file
    protected static Properties loadProperties() {
        Properties props = new Properties();

        // check for user-provided properties file via JVM property and return
        String propsLocation = System.getProperty(FUZZER_PROPS);
        if (propsLocation != null) {
            if (propertiesCache.containsKey(propsLocation)) {
                LOGGER.debug("Loaded cached properties of " + FUZZER_PROPS);
                return propertiesCache.get(propsLocation);
            }

            try {
                props.load(new FileReader(propsLocation));
                propertiesCache.put(propsLocation, props);
                LOGGER.debug("Loaded properties from " + propsLocation);
                return props;
            } catch (IOException e) {
                throw new RuntimeException("Could not load properties from " + propsLocation + ": " + e.getMessage());
            }
        }

        // check for the default properties file and return
        ClassLoader classLoader = PropertyResolver.class.getClassLoader();
        String defaultPropsLocation = classLoader.getResource(DEFAULT_FUZZER_PROPS).toString();
        InputStream defaultPropsStream = classLoader.getResourceAsStream(DEFAULT_FUZZER_PROPS);
        if (defaultPropsStream != null) {
            if (propertiesCache.containsKey(DEFAULT_FUZZER_PROPS)) {
                LOGGER.debug("Loaded cached properties of " + DEFAULT_FUZZER_PROPS);
                return propertiesCache.get(DEFAULT_FUZZER_PROPS);
            }

            try {
                props.load(defaultPropsStream);
                propertiesCache.put(DEFAULT_FUZZER_PROPS, props);
                LOGGER.debug("Loaded properties from " + defaultPropsLocation);
                return props;
            } catch (IOException e) {
                throw new RuntimeException("Could not load properties from " + defaultPropsLocation + ": " + e.getMessage());
            }
        }

        LOGGER.debug("No properties loaded");
        return props;
    }

    // Resolves are the application properties in a given user string.
    public static String resolve(String userString) {
        if (userString == null) {
            return null;
        }

        if (resolutionCache.containsKey(userString)) {
            return resolutionCache.get(userString);
        }

        boolean neededResolution = false;
        StringBuilder resolvedSB = new StringBuilder();
        // pattern starts with '${' then follows anything other than '$' and ends with '}'
        Matcher matcher = Pattern.compile("(\\$\\{)([^\\$]*)(\\})").matcher(userString);

        while (matcher.find()) {
            if (!neededResolution) {
                neededResolution = true;
            }

            // replace found pattern '${prop_key}' with the prop_key's value
            // first dynamic props are searched for the prop_key and then default props
            // if the provided prop_key is unknown, the pattern is not resolved
            String replacement = null;
            if (!dynamicProps.isEmpty() && dynamicProps.containsKey(matcher.group(2))) {
                replacement = dynamicProps.get(matcher.group(2));
            } else if (defaultProps.containsKey(matcher.group(2))) {
                replacement = defaultProps.get(matcher.group(2));
            }

            if (replacement != null) {
                matcher.appendReplacement(resolvedSB, replacement);
            }
        }

        // append remaining characters from userString to resolvedSB
        matcher.appendTail(resolvedSB);

        // in case of no resolution, resolvedString has the same value as userString
        String resolvedString = resolvedSB.toString();
        if (neededResolution) {
            resolutionCache.put(userString, resolvedString);
        }
        return resolvedString;
    }
}
