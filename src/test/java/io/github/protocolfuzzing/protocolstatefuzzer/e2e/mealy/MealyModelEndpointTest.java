package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MealyModelEndpointTest {

    @TempDir
    Path tempDir;

    private static final Map<String, Scenario> SCENARIOS = Scenario.discover(MealyScenarioRun.SCENARIOS).stream()
        .collect(Collectors.toMap(Scenario::getId, Function.identity()));

    private static MealyModelEndpoint endpoint(String scenario) {
        return MealyModelEndpoint.fromScenario(SCENARIOS.get(scenario));
    }

    private static Optional<String> send(MealyModelEndpoint endpoint, String message) {
        return endpoint.onMessage(message.getBytes(StandardCharsets.UTF_8))
            .map(payload -> new String(payload, StandardCharsets.UTF_8));
    }

    private static void assertExchanges(MealyModelEndpoint endpoint, String... messageResponsePairs) {
        for (int i = 0; i < messageResponsePairs.length; i += 2) {
            String expected = messageResponsePairs[i + 1];
            Assertions.assertEquals(Optional.ofNullable(expected), send(endpoint, messageResponsePairs[i]),
                "response to " + messageResponsePairs[i]);
        }
    }

    @Test
    public void server_happyPathAndBug() {
        MealyModelEndpoint server = endpoint("auth-simple/server-buggy");
        Assertions.assertEquals(Optional.empty(), server.onConnect());
        assertExchanges(server, "SREQ", null, "AUTH", "ACK", "SREQ", "SRES", "ACK", null, "SREQ", "SRES");

        server.reset();
        assertExchanges(server, "ACK", "SRES", "AUTH", "ACK", "SREQ", "ACK", "SRES", "ACK");
        Assertions.assertFalse(server.isClosed());
    }

    @Test
    public void client_initialMessageAndClose() {
        MealyModelEndpoint client = endpoint("auth-simple/client-buggy");
        Assertions.assertEquals(Optional.of("AUTH"),
            client.onConnect().map(payload -> new String(payload, StandardCharsets.UTF_8)));
        assertExchanges(client, "AUTH", null, "ACK", "SREQ", "ACK", null);
        Assertions.assertFalse(client.isClosed());

        assertExchanges(client, "SRES", null);
        Assertions.assertTrue(client.isClosed());
        assertExchanges(client, "ACK", null);
        Assertions.assertTrue(client.isClosed());
    }

    @Test
    public void client_bugAndReset() {
        MealyModelEndpoint client = endpoint("auth-simple/client-buggy");
        assertExchanges(client, "SRES", "SREQ", "SRES", null);
        Assertions.assertTrue(client.isClosed());

        client.reset();
        Assertions.assertFalse(client.isClosed());
        assertExchanges(client, "ACK", "SREQ");
    }

    @Test
    public void initialMessage_fromStartEdge() {
        Assertions.assertEquals(Optional.of("AUTH"),
            MealyModelEndpoint
                .parseInitialMessage(
                    SCENARIOS.get("auth-simple/client-buggy").requireFile(MealyModelEndpoint.MODEL_FILE)));
        Assertions.assertEquals(Optional.empty(),
            MealyModelEndpoint
                .parseInitialMessage(
                    SCENARIOS.get("auth-simple/server-buggy").requireFile(MealyModelEndpoint.MODEL_FILE)));
    }

    @Test
    public void initialMessage_ignoresCommentsAndUnlabeledStartEdge() throws IOException {
        Path dotFile = tempDir.resolve("model.dot");
        Files.writeString(dotFile, "/* __start0 -> s0 [label=\"- / HELLO\"] */\ndigraph g {\n__start0 -> s0\n}\n",
            StandardCharsets.UTF_8);
        Assertions.assertEquals(Optional.empty(), MealyModelEndpoint.parseInitialMessage(dotFile));
    }

    @Test
    public void initialMessage_invalidStartEdgeLabel() throws IOException {
        Path dotFile = tempDir.resolve("model.dot");
        Files.writeString(dotFile, "digraph g {\n__start0 -> s0 [label=\"AUTH / ACK\"]\n}\n",
            StandardCharsets.UTF_8);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MealyModelEndpoint.parseInitialMessage(dotFile));
    }

    @Test
    public void unknownMessage_ignored() {
        MealyModelEndpoint server = endpoint("auth-simple/server-buggy");
        assertExchanges(server, "HELLO", null, "AUTH", "ACK");
    }

    @Test
    public void incompleteModel_rejected() {
        CompactMealy<String, String> model = AutomatonBuilders.<String, String>newMealy(Alphabets.fromArray("A", "B"))
            .withInitial("s0")
            .from("s0").on("A").withOutput("X").loop()
            .create();
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MealyModelEndpoint(model, null));
    }

    @Test
    public void reopenedConnection_rejected() {
        CompactMealy<String, String> model = AutomatonBuilders.<String, String>newMealy(Alphabets.fromArray("A"))
            .withInitial("s0")
            .from("s0").on("A").withOutput("SOCKET_CLOSED").to("s1")
            .from("s1").on("A").withOutput("X").loop()
            .create();
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MealyModelEndpoint(model, null));
    }
}
