package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULAdapter;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

import java.util.Collection;

/**
 * A SUL reaching its endpoint through an {@link InMemoryConnector}, built
 * like the SUL of a real RA fuzzer but without a process or sockets.
 * <p>
 * Each test opens a new connection, which resets the endpoint. The SUL knows
 * the message formats from the outputs of the alphabet, as a real fuzzer knows
 * those of its protocol.
 */
public class InMemoryRaSUL implements AbstractSUL<PSymbolInstance, PSymbolInstance, RaExecutionContext> {

    private final SULConfig sulConfig;
    private final CleanupTasks cleanupTasks;
    private final InMemoryConnector connector;
    private final MapperComposerRA<PSymbolInstance, String, RaExecutionContext, Void> mapper;
    private RaExecutionContext context;

    public InMemoryRaSUL(SULConfig sulConfig, CleanupTasks cleanupTasks, InMemoryConnector connector,
        Collection<OutputSymbol> outputs) {
        this.sulConfig = sulConfig;
        this.cleanupTasks = cleanupTasks;
        this.connector = connector;
        RaOutputChecker outputChecker = new RaOutputChecker();
        this.mapper = new MapperComposerRA<>(
            new RaInputMapper(sulConfig.getMapperConfig(), outputChecker, connector),
            new RaOutputMapper(sulConfig.getMapperConfig(), new RaOutputBuilder(), connector, outputs));
    }

    @Override
    public void pre() {
        connector.open();
        context = new RaExecutionContext();
    }

    @Override
    public void post() {}

    @Override
    public PSymbolInstance step(PSymbolInstance input) {
        context.addStepContext();
        context.setReceiveTimeout(sulConfig.getResponseWait());

        PSymbolInstance output = mapper.execute(input, context);
        context.setOutput(output);
        if (mapper.getOutputChecker().isDisabled(output)) {
            context.disableExecution();
        }
        return output;
    }

    @Override
    public SULConfig getSULConfig() {
        return sulConfig;
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    @Override
    public MapperComposerRA<PSymbolInstance, String, RaExecutionContext, Void> getMapper() {
        return mapper;
    }

    @Override
    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
        throw new UnsupportedOperationException("No dynamic port provider for an in-memory SUL");
    }

    @Override
    public DynamicPortProvider getDynamicPortProvider() {
        throw new UnsupportedOperationException("No dynamic port provider for an in-memory SUL");
    }

    @Override
    public SULAdapter getSULAdapter() {
        throw new UnsupportedOperationException("No SUL adapter for an in-memory SUL");
    }
}
