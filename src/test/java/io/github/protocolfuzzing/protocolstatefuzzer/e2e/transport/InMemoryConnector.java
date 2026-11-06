package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Connects a mapper to an {@link Endpoint} in memory, in place of a socket
 * connector.
 * <p>
 * Sending delivers the payload to the endpoint synchronously and queues its
 * response. Receiving takes the next queued response, or fails with a timeout
 * if there is none, or with closed if the endpoint has closed the connection.
 * A response that the {@link SimulatedLatency} does not deliver within the
 * timeout is lost: it is taken from the queue and the receive fails as if
 * there were none.
 */
public class InMemoryConnector {

    public static class TimeoutException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class ClosedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private final Endpoint endpoint;
    private final SimulatedLatency simulatedLatency;
    private final Deque<byte[]> received;

    public InMemoryConnector(Endpoint endpoint, SimulatedLatency simulatedLatency) {
        this.endpoint = endpoint;
        this.simulatedLatency = simulatedLatency;
        this.received = new ArrayDeque<>();
    }

    /**
     * Resets the endpoint and opens a new connection, queuing any payload the
     * endpoint sends first.
     */
    public void open() {
        received.clear();
        endpoint.reset();
        enqueue(endpoint.onConnect());
    }

    /**
     * Delivers a payload to the endpoint and queues its response; payloads
     * sent on a closed connection are dropped.
     *
     * @param payload the payload to send
     */
    public void send(byte[] payload) {
        if (!endpoint.isClosed()) {
            enqueue(endpoint.onMessage(payload));
        }
    }

    /**
     * Receives the next queued response.
     *
     * @param  timeout          the receive timeout in ms
     *
     * @return                  the received payload
     *
     * @throws TimeoutException if no response arrives within the timeout
     * @throws ClosedException  if the endpoint has closed the connection
     */
    public byte[] receive(long timeout) throws TimeoutException, ClosedException {
        byte[] payload = received.poll();
        if (payload != null && simulatedLatency.delivers(timeout)) {
            return payload;
        }
        if (endpoint.isClosed()) {
            throw new ClosedException();
        }
        throw new TimeoutException();
    }

    private void enqueue(Optional<byte[]> payload) {
        payload.ifPresent(received::add);
    }
}
