package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

import java.util.Optional;

/**
 * The server or client side of a protocol, reached through an {@link InMemoryConnector}.
 * <p>
 * Endpoints exchange raw payloads; the wire format is up to each implementation.
 */
public interface Endpoint {

    /**
     * Resets the endpoint to its initial state, as if it was restarted.
     */
    void reset();

    /**
     * Called when a connection is established.
     *
     * @return the payload the endpoint sends first, if any
     */
    Optional<byte[]> onConnect();

    /**
     * Processes a received payload.
     *
     * @param  payload the received payload
     *
     * @return         the response payload, if any
     */
    Optional<byte[]> onMessage(byte[] payload);

    /**
     * Indicates whether the endpoint has closed the connection.
     *
     * @return {@code true} if the connection is closed
     */
    boolean isClosed();
}
