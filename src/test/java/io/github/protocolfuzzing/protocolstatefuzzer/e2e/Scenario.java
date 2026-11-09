package io.github.protocolfuzzing.protocolstatefuzzer.e2e;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * An end-to-end test scenario, a directory holding the model the fake endpoint
 * executes, which is also the expected learned model, and a
 * {@value #PROPERTIES_FILE} with the settings below. A scenario is one variant
 * of a protocol, in the protocol's directory.
 * <ul>
 * <li>{@code role}: {@code server} or {@code client}, the role of the endpoint
 * being learned (required)</li>
 * <li>{@code latency}: simulated response latency in ms (optional, default
 * 0)</li>
 * </ul>
 * See {@code src/test/resources/e2e/README.md} for the full format.
 */
public final class Scenario {

    public static final String PROPERTIES_FILE = "scenario.properties";

    public enum Role {
        SERVER,
        CLIENT
    }

    /**
     * The scenarios of one protocol and role, whose models can be compared.
     *
     * @param protocol the protocol
     * @param role     the role
     */
    public record Group(String protocol, Role role) {

        /**
         * Returns the relative path of the group's directory.
         *
         * @return {@code <protocol>/<role>}, with the role in lowercase
         */
        public String path() {
            return protocol + "/" + role.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return path();
        }
    }

    private final Path dir;
    private final String protocol;
    private final String name;
    private final Role role;
    private final long latency;

    private Scenario(Path dir, Properties properties) {
        this.dir = dir;
        Path fileName = dir.getFileName();
        this.name = fileName == null ? dir.toString() : fileName.toString();
        Path protocolDir = dir.getParent();
        Path protocolName = protocolDir == null ? null : protocolDir.getFileName();
        this.protocol = protocolName == null ? "" : protocolName.toString();
        this.role = parseRole(properties.getProperty("role"));
        this.latency = parseLatency(properties.getProperty("latency", "0"));
    }

    /**
     * Loads a scenario from its directory.
     *
     * @param  dir the scenario directory
     *
     * @return     the loaded scenario
     */
    public static Scenario load(Path dir) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(dir.resolve(PROPERTIES_FILE), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + PROPERTIES_FILE + " of scenario " + dir, e);
        }
        try {
            return new Scenario(dir, properties);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid scenario " + dir + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads all scenarios under a classpath directory, like {@link #discover(Path)}.
     *
     * @param  resourceRoot             the classpath directory, e.g. {@code e2e/mealy}
     *
     * @return                          the scenarios, sorted by id
     *
     * @throws IllegalArgumentException if a scenario is not in a protocol directory
     */
    public static List<Scenario> discover(String resourceRoot) {
        return discover(classpathDir(resourceRoot));
    }

    /**
     * Loads the scenario a SUL is built for, from its {@code -processDir}.
     * The scenario directory represents the implementation a real fuzzer points to.
     *
     * @param  sulConfig the SUL configuration
     *
     * @return           the scenario
     */
    public static Scenario fromProcessDir(SULConfig sulConfig) {
        if (sulConfig.getProcessDir() == null) {
            throw new IllegalArgumentException("No -processDir pointing to the scenario directory");
        }
        return load(Path.of(sulConfig.getProcessDir()));
    }

    /**
     * Resolves a directory on the classpath, such as the test resources.
     *
     * @param  name                     the directory, e.g. {@code e2e/mealy}
     *
     * @return                          the directory
     *
     * @throws IllegalArgumentException if there is no such directory
     */
    public static Path classpathDir(String name) {
        URL url = Scenario.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("No " + name + " on the classpath");
        }
        try {
            return Path.of(url.toURI());
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Loads all scenarios under a directory, in {@code <protocol>/<scenario>/},
     * sorted by id.
     *
     * @param  root                     the directory
     *
     * @return                          the scenarios, one per protocol subdirectory holding a
     *                                      {@value #PROPERTIES_FILE}
     *
     * @throws IllegalArgumentException if a scenario is not in a protocol
     *                                      directory
     */
    public static List<Scenario> discover(Path root) {
        try {
            List<Scenario> scenarios = new ArrayList<>();
            for (Path protocolDir: subdirectories(root)) {
                if (isScenario(protocolDir)) {
                    throw new IllegalArgumentException("Scenario " + protocolDir + " is not in a protocol directory");
                }
                subdirectories(protocolDir).stream().filter(Scenario::isScenario).map(Scenario::load)
                    .forEach(scenarios::add);
            }
            // not the directory order, which differs for protocols named by a prefix of another, e.g. p and p-x
            scenarios.sort(Comparator.comparing(Scenario::getId));
            return scenarios;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the scenario's id, unique among the scenarios of its kind, Mealy
     * or RA, since the same protocol and name can be in both.
     *
     * @return {@code <protocol>/<name>}
     */
    public String getId() {
        return protocol + "/" + name;
    }

    public Path getDir() {
        return dir;
    }

    public Role getRole() {
        return role;
    }

    public Group getGroup() {
        return new Group(protocol, role);
    }

    public long getLatency() {
        return latency;
    }

    /**
     * Resolves a file of the scenario.
     *
     * @param  fileName the file name, relative to the scenario directory
     *
     * @return          the path of the file, if it exists
     */
    public Optional<Path> findFile(String fileName) {
        Path file = dir.resolve(fileName);
        return Files.exists(file) ? Optional.of(file) : Optional.empty();
    }

    /**
     * Resolves a file the scenario must have.
     *
     * @param  fileName the file name, relative to the scenario directory
     *
     * @return          the path of the file
     */
    public Path requireFile(String fileName) {
        return findFile(fileName).orElseThrow(
            () -> new IllegalArgumentException("Scenario " + getId() + " has no " + fileName));
    }

    @Override
    public String toString() {
        return getId();
    }

    private static boolean isScenario(Path dir) {
        return Files.isRegularFile(dir.resolve(PROPERTIES_FILE));
    }

    private static List<Path> subdirectories(Path dir) throws IOException {
        try (Stream<Path> paths = Files.list(dir)) {
            return paths.filter(Files::isDirectory).sorted().toList();
        }
    }

    private static Role parseRole(String value) {
        if (value == null) {
            throw new IllegalArgumentException("missing role");
        }
        return Role.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static long parseLatency(String value) {
        long latency = Long.parseLong(value.trim());
        if (latency < 0) {
            throw new IllegalArgumentException("negative latency " + latency);
        }
        return latency;
    }
}
