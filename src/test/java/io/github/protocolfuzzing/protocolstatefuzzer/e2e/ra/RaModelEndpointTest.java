package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
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

public class RaModelEndpointTest {

    @TempDir
    Path tempDir;

    private static final Map<String, Scenario> SCENARIOS = Scenario.discover(RaScenarioRun.SCENARIOS).stream()
        .collect(Collectors.toMap(Scenario::getId, Function.identity()));

    private static RaModelEndpoint endpoint(String scenario) {
        return RaModelEndpoint.fromScenario(SCENARIOS.get(scenario));
    }

    private static Optional<String> send(RaModelEndpoint endpoint, String message) {
        return endpoint.onMessage(message.getBytes(StandardCharsets.UTF_8))
            .map(payload -> new String(payload, StandardCharsets.UTF_8));
    }

    private static void assertExchanges(RaModelEndpoint endpoint, String... messageResponsePairs) {
        for (int i = 0; i < messageResponsePairs.length; i += 2) {
            String expected = messageResponsePairs[i + 1];
            Assertions.assertEquals(Optional.ofNullable(expected), send(endpoint, messageResponsePairs[i]),
                "response to " + messageResponsePairs[i]);
        }
    }

    @Test
    public void authSimpleServer_tokens() {
        RaModelEndpoint server = endpoint("auth-simple/server-fixed");
        Assertions.assertEquals(Optional.empty(), server.onConnect());

        // authenticating again replaces the token
        assertExchanges(server, "SREQ 0", null, "AUTH", "ACK 1", "SREQ 1", "SRES", "SREQ 5", null, "AUTH", "ACK 6",
            "SREQ 1", null, "SREQ 6", "SRES");
    }

    @Test
    public void authSimpleServer_bugAndFix() {
        assertExchanges(endpoint("auth-simple/server-buggy"), "ACK 3", "SRES", "AUTH", "ACK 4", "SREQ 4", "ACK 5");
        assertExchanges(endpoint("auth-simple/server-fixed"), "ACK 3", null, "AUTH", "ACK 4", "SREQ 4", "SRES");
    }

    @Test
    public void authSimpleClient_happyPathCloses() {
        RaModelEndpoint client = endpoint("auth-simple/client-fixed");
        Assertions.assertEquals(Optional.empty(), client.onConnect());
        assertExchanges(client, "ACK 7", "SREQ 7", "ACK 8", null, "SREQ 8", null);
        Assertions.assertFalse(client.isClosed());

        assertExchanges(client, "SRES", null);
        Assertions.assertTrue(client.isClosed());

        client.reset();
        Assertions.assertFalse(client.isClosed());
        assertExchanges(client, "ACK 2", "SREQ 2");
    }

    @Test
    public void authSimpleClient_bugAndFix() {
        // the made-up token is fresh
        assertExchanges(endpoint("auth-simple/client-buggy"), "SRES", "SREQ 0", "ACK 3", null);
        assertExchanges(endpoint("auth-simple/client-fixed"), "SRES", null, "ACK 3", "SREQ 3");
    }

    @Test
    public void reset_restartsModel() {
        RaModelEndpoint server = endpoint("auth-simple/server-fixed");
        assertExchanges(server, "AUTH", "ACK 0", "SREQ 5", null);

        server.reset();
        Assertions.assertFalse(server.isClosed());
        assertExchanges(server, "SREQ 0", null, "AUTH", "ACK 1");
    }

    @Test
    public void invalidMessages_ignored() {
        RaModelEndpoint server = endpoint("auth-simple/server-fixed");
        assertExchanges(server, "HELLO", null, "AUTH 1", null, "SREQ", null, "SREQ 1 2", null, "SREQ x", null,
            "AUTH", "ACK 0");
    }

    @Test
    public void invalidModel_rejected() throws IOException {
        Path xmlFile = tempDir.resolve("model.xml");
        Files.writeString(xmlFile, "<register-automaton>", StandardCharsets.UTF_8);
        Assertions.assertThrows(IllegalArgumentException.class, () -> RaModelEndpoint.parseModel(xmlFile));
    }

    @Test
    public void reopenedConnection_rejected() throws IOException {
        Path xmlFile = tempDir.resolve("model.xml");
        Files.writeString(xmlFile, """
            <register-automaton>
                <alphabet>
                    <inputs><symbol name="MSG"/></inputs>
                    <outputs><symbol name="SOCKET_CLOSED"/><symbol name="ACK"/></outputs>
                </alphabet>
                <constants/>
                <globals/>
                <locations>
                    <location name="l0" initial="true"/><location name="l1"/>
                    <location name="l2"/><location name="l3"/>
                </locations>
                <transitions>
                    <transition from="l0" to="l1" symbol="MSG"/>
                    <transition from="l1" to="l2" symbol="SOCKET_CLOSED"/>
                    <transition from="l2" to="l3" symbol="MSG"/>
                    <transition from="l3" to="l2" symbol="ACK"/>
                </transitions>
            </register-automaton>
            """, StandardCharsets.UTF_8);
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RaModelEndpoint(RaModelEndpoint.parseModel(xmlFile)));
    }

    @Test
    public void missingTransition_rejected() throws IOException {
        Path xmlFile = tempDir.resolve("model.xml");
        Files.writeString(xmlFile, """
            <register-automaton>
                <alphabet>
                    <inputs><symbol name="MSG"><param type="id" name="p"/></symbol></inputs>
                    <outputs><symbol name="ACK"/></outputs>
                </alphabet>
                <constants/>
                <globals><variable type="id" name="r">0</variable></globals>
                <locations><location name="l0" initial="true"/><location name="l1"/></locations>
                <transitions>
                    <transition from="l0" to="l1" symbol="MSG" params="p"><guard>p==r</guard></transition>
                    <transition from="l1" to="l0" symbol="ACK"/>
                </transitions>
            </register-automaton>
            """, StandardCharsets.UTF_8);
        RaModelEndpoint endpoint = new RaModelEndpoint(RaModelEndpoint.parseModel(xmlFile));

        assertExchanges(endpoint, "MSG 0", "ACK");
        Assertions.assertThrows(IllegalStateException.class, () -> send(endpoint, "MSG 1"));
    }
}
