package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.SimulatedLatency;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

import java.util.Collection;
import java.util.List;

/**
 * Builds {@link InMemoryRaSUL}s, each connected to its own endpoint executing
 * the model of the scenario in {@code -processDir}, as
 * {@code InMemoryMealySULBuilder} does for Mealy scenarios.
 */
public final class InMemoryRaSULBuilder implements SULBuilder<PSymbolInstance, PSymbolInstance, RaExecutionContext> {

    private final List<OutputSymbol> outputs;

    /**
     * Creates a builder of SULs exchanging the given symbols.
     *
     * @param alphabet the inputs and outputs
     */
    public InMemoryRaSULBuilder(Collection<ParameterizedSymbol> alphabet) {
        this.outputs = alphabet.stream().filter(OutputSymbol.class::isInstance).map(OutputSymbol.class::cast)
            .toList();
    }

    @Override
    public AbstractSUL<PSymbolInstance, PSymbolInstance, RaExecutionContext> buildSUL(SULConfig sulConfig,
        CleanupTasks cleanupTasks) {
        Scenario scenario = Scenario.fromProcessDir(sulConfig);
        InMemoryConnector connector = new InMemoryConnector(RaModelEndpoint.fromScenario(scenario),
            new SimulatedLatency(scenario.getLatency()));
        return new InMemoryRaSUL(sulConfig, cleanupTasks, connector, outputs);
    }

    @Override
    public SULWrapper<PSymbolInstance, PSymbolInstance, RaExecutionContext> buildWrapper() {
        return new SULWrapperStandard<>();
    }
}
