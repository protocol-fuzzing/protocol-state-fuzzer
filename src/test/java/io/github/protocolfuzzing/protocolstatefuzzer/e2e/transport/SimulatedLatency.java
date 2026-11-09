package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

/**
 * Simulates an endpoint that takes {@code latency} ms to respond, without sleeping.
 * <p>
 * A response is delivered if the receive timeout is at least the latency.
 * Otherwise it is delivered on every other attempt to receive one and lost on
 * the rest, as with a real SUL that is waited on too briefly. The attempts are
 * counted across tests and runs, so the repeated runs of a test receiving an
 * odd number of responses lose different ones and disagree, while those of a test
 * receiving an even number lose the same ones and agree.
 */
public class SimulatedLatency {

    public static final SimulatedLatency NONE = new SimulatedLatency(0);

    private final long latency;
    private long shortWaits;

    public SimulatedLatency(long latency) {
        this.latency = latency;
        this.shortWaits = 0;
    }

    public boolean delivers(long receiveTimeout) {
        if (receiveTimeout >= latency) {
            return true;
        }
        return shortWaits++ % 2 == 0;
    }
}
