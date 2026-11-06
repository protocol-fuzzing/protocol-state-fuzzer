package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

public class MessageOutputBuilder extends OutputBuilder<MessageOutput> {

    @Override
    public MessageOutput buildOutputExact(String name) {
        return new MessageOutput(name);
    }
}
