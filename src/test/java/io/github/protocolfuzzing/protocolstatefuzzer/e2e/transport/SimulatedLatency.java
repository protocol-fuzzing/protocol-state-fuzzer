package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

/**
 * Simulates an endpoint that takes up to {@code latency} ms to respond, without sleeping.
 * <p>
 * A response is delivered if the receive timeout is at least the latency.
 * Otherwise it is late, and every {@code dropEvery}-th late response is lost
 * and the others are delivered, as with a real SUL that is waited on too
 * briefly. The late responses are counted across tests and runs, so for
 * {@code dropEvery} of 2 the repeated runs of a test receiving an odd number of
 * responses lose different ones and disagree, while those of a test receiving
 * an even number lose the same ones and agree.
 */
public final class SimulatedLatency {

    /** The default {@code dropEvery}: the first late response is delivered, the second is lost, and so on. */
    public static final int DEFAULT_DROP_EVERY = 2;

    public static final SimulatedLatency NONE = new SimulatedLatency(0);

    private final long latency;
    private final int dropEvery;
    private long lateResponses;

    /**
     * Creates a simulated latency that loses every {@value #DEFAULT_DROP_EVERY} late response.
     *
     * @param latency the latency in ms
     */
    public SimulatedLatency(long latency) {
        this(latency, DEFAULT_DROP_EVERY);
    }

    /**
     * Creates a simulated latency.
     *
     * @param latency   the latency in ms
     * @param dropEvery which late responses are lost: every {@code dropEvery}-th one, so 1 loses all of them
     */
    public SimulatedLatency(long latency, int dropEvery) {
        if (dropEvery < 1) {
            throw new IllegalArgumentException("dropEvery must be at least 1: " + dropEvery);
        }
        this.latency = latency;
        this.dropEvery = dropEvery;
        this.lateResponses = 0;
    }

    /**
     * Decides whether a response reaches the receiver.
     *
     * @param  receiveTimeout the receive timeout in ms
     *
     * @return                {@code true} if the response is delivered, {@code false} if it is lost
     */
    public boolean delivers(long receiveTimeout) {
        if (receiveTimeout >= latency) {
            return true;
        }
        return ++lateResponses % dropEvery != 0;
    }
}
