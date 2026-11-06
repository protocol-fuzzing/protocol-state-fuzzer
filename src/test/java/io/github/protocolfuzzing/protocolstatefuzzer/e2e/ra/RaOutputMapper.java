package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.data.FreshValue;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapperRA;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Receives a message over the connector and maps it to the output of the same
 * name with its data values, or to TIMEOUT, SOCKET_CLOSED or UNKNOWN.
 * <p>
 * A data value new to the connection is a {@link FreshValue}, as RALib
 * expects fresh output values to be marked.
 */
public class RaOutputMapper extends OutputMapperRA<PSymbolInstance, String, RaExecutionContext> {

    private final InMemoryConnector connector;
    private final RaMessages<OutputSymbol> outputs;

    public RaOutputMapper(MapperConfig mapperConfig, OutputBuilder<PSymbolInstance> outputBuilder,
        InMemoryConnector connector, Collection<OutputSymbol> outputs) {
        super(mapperConfig, outputBuilder);
        this.connector = connector;
        this.outputs = new RaMessages<>(outputs);
    }

    @Override
    public PSymbolInstance receiveOutput(RaExecutionContext context) {
        byte[] payload;
        try {
            payload = connector.receive(context.getReceiveTimeout());
        }
        catch (InMemoryConnector.TimeoutException e) {
            return timeout();
        }
        catch (InMemoryConnector.ClosedException e) {
            return socketClosed();
        }

        Optional<PSymbolInstance> message = outputs.decode(new String(payload, StandardCharsets.UTF_8));
        if (message.isEmpty()) {
            return outputBuilder.buildUnknown();
        }
        DataValue[] values = message.get().getParameterValues().clone();
        for (int i = 0; i < values.length; i++) {
            if (context.addValue(values[i])) {
                values[i] = new FreshValue(values[i].getDataType(), values[i].getValue());
            }
        }
        return new PSymbolInstance(message.get().getBaseSymbol(), values);
    }

    @Override
    protected PSymbolInstance buildOutput(String name, List<String> messages) {
        return outputBuilder.buildOutput(name);
    }
}
