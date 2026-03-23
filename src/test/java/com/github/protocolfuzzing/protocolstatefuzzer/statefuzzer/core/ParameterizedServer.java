package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import java.math.BigDecimal;
import java.util.Random;

public class ParameterizedServer {
    private static final Integer MAX_MSG_ID = Integer.MAX_VALUE;
    public static record Msg(BigDecimal msgId) {};
    public static record Ack(BigDecimal nextMsgId) {};

    private BigDecimal nextMsgId;
    private Random rand;

    public ParameterizedServer() {
        nextMsgId = null;
        rand = new Random(42);
    }

    public Ack send(Msg m) {
        if (nextMsgId == null || nextMsgId.compareTo(m.msgId()) == 0) {
            nextMsgId = new BigDecimal(rand.nextInt(MAX_MSG_ID));
            return new Ack(nextMsgId);
        }

        return null;
    }
}
