package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;

import java.util.List;

/**
 * An output symbol, named after the received message or a special output
 * such as TIMEOUT.
 */
public class MessageOutput extends AbstractOutput<MessageOutput, String> {

    public MessageOutput(String name) {
        super(name);
    }

    public MessageOutput(String name, List<String> messages) {
        super(name, messages);
    }

    @Override
    protected MessageOutput buildOutput(String name) {
        return new MessageOutput(name);
    }

    @Override
    protected MessageOutput convertOutput() {
        return new MessageOutput(name, messages);
    }
}
