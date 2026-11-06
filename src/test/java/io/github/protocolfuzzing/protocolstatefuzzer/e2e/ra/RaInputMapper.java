package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapperRA;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;

import java.nio.charset.StandardCharsets;

/**
 * Sends an input over the connector as {@link RaMessages}, recording its data
 * values in the context.
 */
public class RaInputMapper extends InputMapperRA<PSymbolInstance, String, RaExecutionContext> {

    private final InMemoryConnector connector;

    public RaInputMapper(MapperConfig mapperConfig, OutputChecker<PSymbolInstance> outputChecker,
        InMemoryConnector connector) {
        super(mapperConfig, outputChecker);
        this.connector = connector;
    }

    @Override
    public void preSendUpdate(PSymbolInstance input, RaExecutionContext context) {
        for (DataValue value: input.getParameterValues()) {
            context.addValue(value);
        }
    }

    @Override
    public String generateProtocolMessage(PSymbolInstance input, RaExecutionContext context) {
        return RaMessages.encode(input);
    }

    @Override
    public void sendMessage(String message, RaExecutionContext context) {
        connector.send(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void postSendUpdate(PSymbolInstance input, RaExecutionContext context) {}

    @Override
    public void postReceiveUpdate(PSymbolInstance input, PSymbolInstance output,
        OutputChecker<PSymbolInstance> outputChecker, RaExecutionContext context) {}
}
