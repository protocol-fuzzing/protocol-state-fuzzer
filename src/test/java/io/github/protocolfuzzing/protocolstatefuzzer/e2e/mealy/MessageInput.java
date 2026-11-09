package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInputXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;

/**
 * An input symbol sending the message of the same name, declared in the
 * alphabet as {@code <Input name="AUTH"/>}.
 */
public class MessageInput extends AbstractInputXml<MessageOutput, String, MealyExecutionContext> {

    public MessageInput() {
        super();
    }

    public MessageInput(String name) {
        super(name);
    }

    @Override
    public void preSendUpdate(MealyExecutionContext context) {}

    @Override
    public String generateProtocolMessage(MealyExecutionContext context) {
        return getName();
    }

    @Override
    public void postSendUpdate(MealyExecutionContext context) {}

    @Override
    public void postReceiveUpdate(MessageOutput output, OutputChecker<MessageOutput> outputChecker,
        MealyExecutionContext context) {}
}
