package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

import com.beust.jcommander.DynamicParameter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It is responsible for the loading of the initial application properties,
 * the storing of the dynamically provided properties and the resolution
 * of properties during the argument parsing.
 */
public class PropertyResolver {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The default filename that should be in resources containing initial application properties. */
    public static final String DEFAULT_FUZZER_PROPS_FILE = "default_fuzzer.properties";

    /**
     * The property that can contain a custom filename with initial application properties;
     * which can be defined only as a JVM/system property: {@code -Dfuzzer.properties=file}.
     */
    public static final String FUZZER_PROPS = "fuzzer.properties";

    /** The property that can contain a custom current working directory. */
    public static final String FUZZER_DIR = "fuzzer.dir";

    /** The property containing a randomly generated port between 10000 and 39999. */
    public static final String SUL_PORT = "sul.port";

    /** The property containing a randomly generated port between 40000 and 65535. */
    public static final String FUZZER_PORT = "fuzzer.port";

    /** The property containing a new generated timestamp. */
    public static final String TIMESTAMP = "timestamp";

    /** The property containing the format of the generated timestamp. */
    public static final String TIMESTAMP_FORMAT = "timestamp.format";

    /**
     * Stores default application properties as provided in the {@link #FUZZER_PROPS}
     * or the {@link #DEFAULT_FUZZER_PROPS_FILE}.
     * <p>
     * The properties contained in those files can be overwritten by the same
     * ones redefined as JVM/system properties (after the java command, before -jar)
     * or overshadowed during resolution by the same ones redefined as dynamic
     * properties in {@link #dynamicProps}.
     * <p>
     * The resolution priority (in decreasing order) is:
     *  dynamic application properties {@literal >}
     *  JVM/system properties {@literal >}
     *  initial property from file.
     */
    protected static final Map<String, String> defaultProps = new LinkedHashMap<>();

    /**
     * Stores dynamically provided application properties which include variable definitions.
     * <p>
     * They are defined as normal arguments and they are not JVM/system properties.
     * <p>
     * A variable definition can be {@code -Dvar=varValue}. In the arguments
     * the var variable can be used as ${var}. Variables are replaced with their
     * corresponding values before the arguments are parsed. In this example the
     * ${var} will be replaced with varValue.
     */
    @SuppressFBWarnings
    @DynamicParameter(names = "-D", description = "Definitions for variables, which can be used as ${var}. "
            + "Variables are replaced with their corresponding values before the arguments are parsed.")
    protected static Map<String, String> dynamicProps = new LinkedHashMap<>();

    /** Stores loaded properties to avoid reloading them on next invocation of {@link #loadProperties()}. */
    protected static final Map<String, Properties> propertiesCache = new LinkedHashMap<>();

    /** Caches resolved userStrings to avoid reparsing them if they occur again. */
    protected static final Map<String, String> resolutionCache = new HashMap<>();

    // singleton instance (lazily initialized)
    private static PropertyResolver instance;

    // private constructor to avoid client applications using the constructor
    private PropertyResolver() {}

    /**
     * Returns the singleton instance of PropertyResolver.
     *
     * @return  the singleton instance of PropertyResolver
     */
    public static synchronized PropertyResolver getInstance() {
        if (instance == null) {
            instance = new PropertyResolver();
        }
        return instance;
    }

    /**
     * Loads the initial properties using {@link #loadProperties()} and initializes
     * the {@link #defaultProps}; should be invoked before the argument parsing.
     * <p>
     * If a property is also provided as a JVM property then the JVM property
     * is stored in {@link #defaultProps}.
     */
    public static void initializeParsing() {

        Properties fileLoadedProps = loadProperties();

        // add fileLoadedProps properties to defaultProps
        // system defined properties have priority over them
        for (String propName : fileLoadedProps.stringPropertyNames()) {
            String fileProp = fileLoadedProps.getProperty(propName);
            String defaultProp = System.getProperty(propName, fileProp);
            defaultProps.put(propName, defaultProp);
        }

        // If some properties are not in fileLoadedProps, check if they are present in
        // JVM/system properties and add a default value if they are not

        // Current working directory
        if (!fileLoadedProps.containsKey(FUZZER_DIR)) {
            String userDir = System.getProperty("user.dir");
            String fuzzerDir = System.getProperty(FUZZER_DIR, userDir);
            defaultProps.put(FUZZER_DIR, fuzzerDir);
        }

        // SUL port between 10000 and 39999
        if (!fileLoadedProps.containsKey(SUL_PORT)) {
            long sulSec = (System.currentTimeMillis() / 1000 % 30000) + 10000;
            String sulPort = System.getProperty(SUL_PORT, Long.toString(sulSec));
            defaultProps.put(SUL_PORT, sulPort);
        }

        // Fuzzer port between 40000 and 65535
        if (!fileLoadedProps.containsKey(FUZZER_PORT)) {
            long fuzzSec = (System.currentTimeMillis() / 1000 % 25536) + 40000;
            String fuzzerPort = System.getProperty(FUZZER_PORT, Long.toString(fuzzSec));
            defaultProps.put(FUZZER_PORT, fuzzerPort);
        }

        // Timestamp format
        String defaultFormat = "yyyy-MM-dd_HH-mm-ss";
        if (!fileLoadedProps.containsKey(TIMESTAMP_FORMAT)) {
            String format = System.getProperty(TIMESTAMP_FORMAT, defaultFormat);
            defaultProps.put(TIMESTAMP_FORMAT, format);
        }

        // Timestamp
        if (!fileLoadedProps.containsKey(TIMESTAMP)) {
            String timestampFormat = defaultProps.getOrDefault(TIMESTAMP_FORMAT, defaultFormat);
            String timestamp = DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now());
            defaultProps.put(TIMESTAMP, timestamp);
        }
    }

    /**
     * Clears the {@link #defaultProps} and {@link #dynamicProps}; should be
     * invoked after the argument parsing.
     * <p>
     * This allows the next parse to have a clean re-initialized state.
     */
    public static void finalizeParsing() {
        defaultProps.clear();
        dynamicProps.clear();
        resolutionCache.clear();
    }

    /**
     * Loads initial properties with the priority search order being
     * {@link #FUZZER_PROPS} and then {@link #DEFAULT_FUZZER_PROPS_FILE}.
     * If no file is available then no initial properties are loaded.
     *
     * @return  the properties that either contain the loaded properties or are empty
     */
    protected static Properties loadProperties() {
        Properties props = new Properties();

        // check for user-provided properties file via JVM property and return
        String propsLocation = System.getProperty(FUZZER_PROPS);
        if (propsLocation != null) {
            if (propertiesCache.containsKey(propsLocation)) {
                LOGGER.trace("Loaded cached properties of " + FUZZER_PROPS);
                return propertiesCache.get(propsLocation);
            }

            try {
                FileReader fileReader = new FileReader(propsLocation, StandardCharsets.UTF_8);
                props.load(fileReader);
                propertiesCache.put(propsLocation, props);
                LOGGER.trace("Loaded properties from " + propsLocation);
                fileReader.close();
                return props;
            } catch (IOException e) {
                throw new RuntimeException("Could not load properties from " + propsLocation + ": " + e.getMessage());
            }
        }

        // check for the default properties file and return
        ClassLoader classLoader = PropertyResolver.class.getClassLoader();
        URL defaultPropsUrl = classLoader.getResource(DEFAULT_FUZZER_PROPS_FILE);
        if (defaultPropsUrl != null) {
            if (propertiesCache.containsKey(DEFAULT_FUZZER_PROPS_FILE)) {
                LOGGER.trace("Loaded cached properties of " + DEFAULT_FUZZER_PROPS_FILE);
                return propertiesCache.get(DEFAULT_FUZZER_PROPS_FILE);
            }

            try (InputStream inputStream = defaultPropsUrl.openStream()) {
                props.load(inputStream);
                propertiesCache.put(DEFAULT_FUZZER_PROPS_FILE, props);
                LOGGER.trace("Loaded properties from " + defaultPropsUrl);
                return props;
            } catch (IOException e) {
                throw new RuntimeException("Could not load properties from " + defaultPropsUrl + ": " + e.getMessage());
            }
        }

        LOGGER.trace("No properties loaded");
        return props;
    }

    /**
     * Resolves all the application properties in a given userString by
     * substituting them for values.
     * <p>
     * An example is the application property 'sul.port', which can  be used as
     * {@code -host localhost:${sul.port}}. If 'sul.port' has value 123
     * and this method is supplied with userString = "localhost:${sul.port}" then
     * the returned string will be "localhost:123".
     * <p>
     * If an application property cannot be resolved then it remains unsubstituted
     * in the returned string. If the userString contains no application properties
     * then the userString is returned.
     *
     * @param userString  the string that may contain application properties
     * @return            the resolved string
     */
    public static String resolve(String userString) {

        // add new timestamp to dynamicProps if
        // there is a timestamp format in dynamicProps
        if (!dynamicProps.containsKey(TIMESTAMP)
            && dynamicProps.containsKey(TIMESTAMP_FORMAT)) {

            String format = dynamicProps.get(TIMESTAMP_FORMAT);
            String timestamp = DateTimeFormatter.ofPattern(format).format(LocalDateTime.now());
            dynamicProps.put(TIMESTAMP, timestamp);
        }

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
            String replacement =
                dynamicProps.getOrDefault(matcher.group(2),
                defaultProps.getOrDefault(matcher.group(2),
                null)
            );

            if (replacement != null) {
                matcher.appendReplacement(resolvedSB, replacement);
            }
        }
        // append remaining characters from userString to resolvedSB
        matcher.appendTail(resolvedSB);

        // in case of no resolution, resultString is the userString
        String resultString = userString;

        if (neededResolution) {
            // in case of resolution, resultString is the resolved string
            resultString = resolvedSB.toString();
            resolutionCache.put(userString, resultString);
        }

        return resultString;
    }
}
