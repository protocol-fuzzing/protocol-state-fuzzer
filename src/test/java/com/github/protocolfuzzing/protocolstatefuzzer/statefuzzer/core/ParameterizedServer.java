package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import java.util.Random;

public class ParameterizedServer {
    private static final Integer MAX_MSG_ID = Integer.MAX_VALUE;
    public static record Msg (Integer msgId) {};
    public static record Ack (Integer nextMsgId) {};

    private Integer nextMsgId;
    private Random rand;

    public ParameterizedServer() {
        nextMsgId = null;
        rand = new Random(1);
    }

    public Ack send(Msg m) {
        if (nextMsgId == null || m.msgId() == nextMsgId) {
            nextMsgId = rand.nextInt(MAX_MSG_ID);
            return new Ack(nextMsgId);
        } else {
            return null;
        }
    }
}
