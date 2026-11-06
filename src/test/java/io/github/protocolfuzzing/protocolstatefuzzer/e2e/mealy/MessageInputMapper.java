package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;

import java.nio.charset.StandardCharsets;

/**
 * Sends the message of an input over the connector.
 */
public class MessageInputMapper extends InputMapper<MessageInput, MessageOutput, String, MealyExecutionContext> {

    private final InMemoryConnector connector;

    public MessageInputMapper(MapperConfig mapperConfig, MessageOutputChecker outputChecker,
        InMemoryConnector connector) {
        super(mapperConfig, outputChecker);
        this.connector = connector;
    }

    @Override
    protected void sendMessage(String message, MealyExecutionContext context) {
        connector.send(message.getBytes(StandardCharsets.UTF_8));
    }
}
