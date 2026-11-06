package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class InMemoryConnectorTest {

    /**
     * Sends "HELLO" on connect, echoes payloads and closes on "BYE".
     */
    private static class EchoEndpoint implements Endpoint {
        int resets = 0;
        boolean closed = false;

        @Override
        public void reset() {
            resets++;
            closed = false;
        }

        @Override
        public Optional<byte[]> onConnect() {
            return Optional.of(bytes("HELLO"));
        }

        @Override
        public Optional<byte[]> onMessage(byte[] payload) {
            if (text(payload).equals("BYE")) {
                closed = true;
                return Optional.empty();
            }
            return Optional.of(payload);
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static String text(byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8);
    }

    private final EchoEndpoint endpoint = new EchoEndpoint();

    @Test
    public void open_resetsEndpointAndQueuesInitialPayload() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, SimulatedLatency.NONE);
        connector.open();
        Assertions.assertEquals(1, endpoint.resets);
        Assertions.assertEquals("HELLO", text(connector.receive(100)));
        Assertions.assertThrows(InMemoryConnector.TimeoutException.class, () -> connector.receive(100));
    }

    @Test
    public void open_discardsPendingPayloads() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, SimulatedLatency.NONE);
        connector.open();
        connector.send(bytes("A"));
        connector.open();
        Assertions.assertEquals("HELLO", text(connector.receive(100)));
        Assertions.assertThrows(InMemoryConnector.TimeoutException.class, () -> connector.receive(100));
    }

    @Test
    public void send_queuesResponsesInOrder() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, SimulatedLatency.NONE);
        connector.open();
        connector.receive(100);
        connector.send(bytes("A"));
        connector.send(bytes("B"));
        Assertions.assertEquals("A", text(connector.receive(100)));
        Assertions.assertEquals("B", text(connector.receive(100)));
    }

    @Test
    public void closed_receiveFailsAndSendIsDropped() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, SimulatedLatency.NONE);
        connector.open();
        connector.receive(100);
        connector.send(bytes("BYE"));
        Assertions.assertThrows(InMemoryConnector.ClosedException.class, () -> connector.receive(100));
        connector.send(bytes("A"));
        Assertions.assertThrows(InMemoryConnector.ClosedException.class, () -> connector.receive(100));
    }

    @Test
    public void latency_longEnoughTimeoutAlwaysDelivers() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, new SimulatedLatency(200));
        for (int i = 0; i < 4; i++) {
            connector.open();
            Assertions.assertEquals("HELLO", text(connector.receive(200)));
        }
    }

    @Test
    public void latency_shortTimeoutAlternates() throws Exception {
        InMemoryConnector connector = new InMemoryConnector(endpoint, new SimulatedLatency(200));
        connector.open();
        Assertions.assertEquals("HELLO", text(connector.receive(199)));
        connector.open();
        Assertions.assertThrows(InMemoryConnector.TimeoutException.class, () -> connector.receive(199));
        connector.open();
        Assertions.assertEquals("HELLO", text(connector.receive(199)));
    }
}
