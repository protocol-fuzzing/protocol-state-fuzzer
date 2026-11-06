package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MealyScenarioRunTest {

    @TempDir
    static Path tempDir;

    private static List<Scenario> scenarios;
    private static Map<String, Scenario> byName;

    /**
     * Writes a one-state scenario of protocol {@code p}, with a transition
     * {@code INPUT / OUTPUT} per edge, in the order given.
     */
    private static void writeScenario(String name, String role, String... edges) throws IOException {
        Path dir = Files.createDirectories(tempDir.resolve("p").resolve(name));
        Files.writeString(dir.resolve(Scenario.PROPERTIES_FILE), "role=" + role, StandardCharsets.UTF_8);
        StringBuilder dot = new StringBuilder("digraph g {\n__start0 [label=\"\" shape=\"none\"]\n__start0 -> s0\n");
        for (String edge: edges) {
            dot.append("s0 -> s0 [label=\"").append(edge).append("\"]\n");
        }
        Files.writeString(dir.resolve(MealyModelEndpoint.MODEL_FILE), dot.append("}\n"), StandardCharsets.UTF_8);
    }

    @BeforeAll
    public static void writeScenarios() throws IOException {
        writeScenario("server-a", "server", "A / X", "B / Y");
        // the same model, with the inputs in another order
        writeScenario("server-reordered", "server", "B / Y", "A / X");
        // the same inputs, another output
        writeScenario("server-inequivalent", "server", "A / X", "B / X");
        // other inputs
        writeScenario("server-other-inputs", "server", "A / X", "C / Y");
        // the same model, another role
        writeScenario("client-a", "client", "A / X", "B / Y");

        scenarios = Scenario.discover(tempDir);
        byName = scenarios.stream().collect(Collectors.toMap(Scenario::getName, Function.identity()));
    }

    @Test
    public void equivalentScenarios_ignoreInputOrder() {
        Assertions.assertEquals(Set.of("server-a", "server-reordered"),
            MealyScenarioRun.equivalentScenarios(byName.get("server-a"), scenarios));
        Assertions.assertEquals(Set.of("server-a", "server-reordered"),
            MealyScenarioRun.equivalentScenarios(byName.get("server-reordered"), scenarios));
    }

    @Test
    public void equivalentScenarios_excludeOtherInputsAndRoles() {
        Assertions.assertEquals(Set.of("server-other-inputs"),
            MealyScenarioRun.equivalentScenarios(byName.get("server-other-inputs"), scenarios));
        Assertions.assertEquals(Set.of("client-a"),
            MealyScenarioRun.equivalentScenarios(byName.get("client-a"), scenarios));
    }

    @Test
    public void comparablePairs_sameGroupAndInputsInAnyOrder() {
        List<List<String>> pairs = MealyScenarioRun.comparablePairs(scenarios).stream()
            .map(pair -> pair.stream().map(Scenario::getName).toList()).toList();
        Assertions.assertEquals(List.of(
            List.of("server-a", "server-inequivalent"),
            List.of("server-a", "server-reordered"),
            List.of("server-inequivalent", "server-reordered")), pairs);
    }
}
