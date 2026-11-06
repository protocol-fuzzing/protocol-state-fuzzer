package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;

public class MessageOutputChecker implements OutputChecker<MessageOutput> {

    /**
     * Any received message counts as the initial message of a client.
     */
    @Override
    public boolean hasInitialClientMessage(MessageOutput output) {
        return !isTimeout(output) && !isUnknown(output) && !isSocketClosed(output) && !isDisabled(output);
    }

    @Override
    public boolean isTimeout(MessageOutput output) {
        return OutputBuilder.TIMEOUT.equals(output.getName());
    }

    @Override
    public boolean isUnknown(MessageOutput output) {
        return OutputBuilder.UNKNOWN.equals(output.getName());
    }

    @Override
    public boolean isSocketClosed(MessageOutput output) {
        return OutputBuilder.SOCKET_CLOSED.equals(output.getName());
    }

    @Override
    public boolean isDisabled(MessageOutput output) {
        return OutputBuilder.DISABLED.equals(output.getName());
    }
}
