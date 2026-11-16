package io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SimulatedLatencyTest {

    private static List<Boolean> deliveries(SimulatedLatency latency, long timeout, int responses) {
        List<Boolean> deliveries = new ArrayList<>();
        for (int i = 0; i < responses; i++) {
            deliveries.add(latency.delivers(timeout));
        }
        return deliveries;
    }

    @Test
    public void delivers_timeoutAtLeastLatencyAlwaysDelivers() {
        SimulatedLatency latency = new SimulatedLatency(200, 1);
        Assertions.assertEquals(List.of(true, true, true), deliveries(latency, 200, 3));
        Assertions.assertEquals(List.of(true, true, true), deliveries(latency, 1000, 3));
    }

    @Test
    public void delivers_defaultLosesEverySecondLateResponse() {
        Assertions.assertEquals(List.of(true, false, true, false), deliveries(new SimulatedLatency(200), 199, 4));
    }

    @Test
    public void delivers_dropEveryOneLosesAllLateResponses() {
        Assertions.assertEquals(List.of(false, false, false), deliveries(new SimulatedLatency(200, 1), 199, 3));
    }

    @Test
    public void delivers_dropEveryThreeLosesEveryThirdLateResponse() {
        Assertions.assertEquals(List.of(true, true, false, true, true, false),
            deliveries(new SimulatedLatency(200, 3), 199, 6));
    }

    @Test
    public void delivers_timelyResponsesAreNotCounted() {
        SimulatedLatency latency = new SimulatedLatency(200);
        Assertions.assertTrue(latency.delivers(199));
        Assertions.assertTrue(latency.delivers(200));
        Assertions.assertFalse(latency.delivers(199));
    }

    @Test
    public void constructor_rejectsDropEveryBelowOne() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SimulatedLatency(200, 0));
    }
}
