package io.github.protocolfuzzing.protocolstatefuzzer.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ScenarioTest {

    @TempDir
    Path tempDir;

    private Path writeScenario(String name, String properties) throws IOException {
        Path dir = Files.createDirectories(tempDir.resolve(name));
        Files.writeString(dir.resolve(Scenario.PROPERTIES_FILE), properties, StandardCharsets.UTF_8);
        return dir;
    }

    @Test
    public void discover_mealyScenarios() {
        List<Scenario> scenarios = Scenario.discover("e2e/mealy");
        List<String> ids = scenarios.stream().map(Scenario::getId).toList();
        Assertions.assertEquals(ids.stream().sorted().toList(), ids, "scenarios not sorted by id");
        Assertions.assertTrue(
            ids.containsAll(List.of("auth-simple/client-buggy", "auth-simple/server-buggy",
                "auth-simple/server-buggy-slow")),
            () -> "found " + ids);

        Scenario client = scenarios.get(ids.indexOf("auth-simple/client-buggy"));
        Assertions.assertEquals("auth-simple", client.getProtocol());
        Assertions.assertEquals("client-buggy", client.getName());
        Assertions.assertEquals(Scenario.Role.CLIENT, client.getRole());
        Assertions.assertEquals(0, client.getLatency());

        Scenario server = scenarios.get(ids.indexOf("auth-simple/server-buggy"));
        Assertions.assertEquals(Scenario.Role.SERVER, server.getRole());

        Scenario slowServer = scenarios.get(ids.indexOf("auth-simple/server-buggy-slow"));
        Assertions.assertEquals(200, slowServer.getLatency());
    }

    @Test
    public void discover_protocolDirectories() throws IOException {
        writeScenario("p2/b", "role=server");
        writeScenario("p1/b", "role=client");
        writeScenario("p1/a", "role=server");
        writeScenario("p1-x/a", "role=server");
        Files.createDirectories(tempDir.resolve("p1/no-scenario"));
        Files.createDirectories(tempDir.resolve("common-args"));
        Files.writeString(tempDir.resolve("common-args/diff-test"), "", StandardCharsets.UTF_8);

        List<Scenario> scenarios = Scenario.discover(tempDir);

        // sorted by id, where p1-x/a comes before p1/a, unlike the directories p1 and p1-x
        Assertions.assertEquals(List.of("p1-x/a", "p1/a", "p1/b", "p2/b"),
            scenarios.stream().map(Scenario::getId).toList());
        Assertions.assertEquals(new Scenario.Group("p1", Scenario.Role.CLIENT), scenarios.get(2).getGroup());
        Assertions.assertEquals("p1/client", scenarios.get(2).getGroup().path());
    }

    @Test
    public void discover_scenarioOutsideProtocol() throws IOException {
        writeScenario("p1/a", "role=server");
        writeScenario("flat", "role=server");
        Assertions.assertThrows(IllegalArgumentException.class, () -> Scenario.discover(tempDir));
    }

    @Test
    public void discover_missingRoot() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Scenario.discover("e2e/nonexistent"));
    }

    @Test
    public void load_allSettings() throws IOException {
        Scenario scenario = Scenario.load(writeScenario("proto/slow", "role=Client\nlatency=250\n"));
        Assertions.assertEquals("slow", scenario.getName());
        Assertions.assertEquals("proto", scenario.getProtocol());
        Assertions.assertEquals("proto/slow", scenario.getId());
        Assertions.assertEquals(Scenario.Role.CLIENT, scenario.getRole());
        Assertions.assertEquals(250, scenario.getLatency());
    }

    @Test
    public void load_invalidSettings() throws IOException {
        for (String properties: new String[] {"", "role=peer", "role=server\nlatency=-1", "role=server\nlatency=x"}) {
            Path dir = writeScenario("invalid" + properties.hashCode(), properties);
            Assertions.assertThrows(IllegalArgumentException.class, () -> Scenario.load(dir), properties);
        }
    }

    @Test
    public void files() throws IOException {
        Path dir = writeScenario("files", "role=server");
        Files.writeString(dir.resolve("model.dot"), "", StandardCharsets.UTF_8);
        Scenario scenario = Scenario.load(dir);

        Assertions.assertEquals(dir.resolve("model.dot"), scenario.requireFile("model.dot"));
        Assertions.assertEquals(Optional.empty(), scenario.findFile("alphabet.xml"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> scenario.requireFile("alphabet.xml"));
    }
}
