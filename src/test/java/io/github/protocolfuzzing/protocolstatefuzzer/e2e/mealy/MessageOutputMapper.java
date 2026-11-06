package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Receives a message over the connector and maps it to the output of the same
 * name, or to TIMEOUT or SOCKET_CLOSED.
 */
public class MessageOutputMapper extends OutputMapper<MessageOutput, String, MealyExecutionContext> {

    private final InMemoryConnector connector;

    public MessageOutputMapper(MapperConfig mapperConfig, MessageOutputBuilder outputBuilder,
        MessageOutputChecker outputChecker, InMemoryConnector connector) {
        super(mapperConfig, outputBuilder, outputChecker);
        this.connector = connector;
    }

    @Override
    public MessageOutput receiveOutput(MealyExecutionContext context) {
        try {
            byte[] payload = connector.receive(context.getReceiveTimeout());
            return outputBuilder.buildOutput(new String(payload, StandardCharsets.UTF_8).trim());
        }
        catch (InMemoryConnector.TimeoutException e) {
            return timeout();
        }
        catch (InMemoryConnector.ClosedException e) {
            return socketClosed();
        }
    }

    @Override
    protected MessageOutput buildOutput(String name, List<String> messages) {
        return new MessageOutput(name, messages);
    }
}
