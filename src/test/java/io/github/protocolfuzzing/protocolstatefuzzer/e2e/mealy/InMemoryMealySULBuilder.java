package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.SimulatedLatency;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

/**
 * Builds {@link InMemoryMealySUL}s, each connected to its own endpoint
 * executing the model of the scenario in {@code -processDir}
 * (see {@link Scenario#fromProcessDir}).
 */
public class InMemoryMealySULBuilder implements SULBuilder<MessageInput, MessageOutput, MealyExecutionContext> {

    @Override
    public AbstractSUL<MessageInput, MessageOutput, MealyExecutionContext> buildSUL(SULConfig sulConfig,
        CleanupTasks cleanupTasks) {
        Scenario scenario = Scenario.fromProcessDir(sulConfig);
        InMemoryConnector connector = new InMemoryConnector(MealyModelEndpoint.fromScenario(scenario),
            new SimulatedLatency(scenario.getLatency()));
        return new InMemoryMealySUL(sulConfig, cleanupTasks, connector);
    }

    @Override
    public SULWrapper<MessageInput, MessageOutput, MealyExecutionContext> buildWrapper() {
        return new SULWrapperStandard<>();
    }
}
