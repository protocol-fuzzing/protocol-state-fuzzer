package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULAdapter;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.InMemoryConnector;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

/**
 * A SUL reaching its endpoint through an {@link InMemoryConnector}, built
 * like the SUL of a real fuzzer but without a process or sockets.
 * <p>
 * Each test opens a new connection, which resets the endpoint. When fuzzing a
 * client, the client's initial message is consumed before the first input, so
 * it does not appear in the learned model.
 */
public class InMemoryMealySUL implements AbstractSUL<MessageInput, MessageOutput, MealyExecutionContext> {

    /**
     * The time in ms to wait for the initial message of a client. It stands
     * for accepting the client's connection, which a real SUL blocks on, and
     * so does not depend on {@code responseWait}.
     */
    public static final long INITIAL_MESSAGE_TIMEOUT = 10_000L;

    private final SULConfig sulConfig;
    private final CleanupTasks cleanupTasks;
    private final InMemoryConnector connector;
    private final MapperComposer<MessageInput, MessageOutput, String, MealyExecutionContext, Void> mapper;
    private MealyExecutionContext context;

    public InMemoryMealySUL(SULConfig sulConfig, CleanupTasks cleanupTasks, InMemoryConnector connector) {
        this.sulConfig = sulConfig;
        this.cleanupTasks = cleanupTasks;
        this.connector = connector;

        MessageOutputChecker outputChecker = new MessageOutputChecker();
        this.mapper = new MapperComposer<>(
            new MessageInputMapper(sulConfig.getMapperConfig(), outputChecker, connector),
            new MessageOutputMapper(sulConfig.getMapperConfig(), new MessageOutputBuilder(), outputChecker,
                connector));
    }

    @Override
    public void pre() {
        connector.open();
        context = new MealyExecutionContext();
        if (sulConfig.isFuzzingClient()) {
            consumeInitialClientMessage();
        }
    }

    @Override
    public void post() {}

    @Override
    public MessageOutput step(MessageInput input) {
        context.addStepContext();
        context.setReceiveTimeout(receiveTimeout(input));

        MessageOutput output = mapper.execute(input, context);
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
    public MapperComposer<MessageInput, MessageOutput, String, MealyExecutionContext, Void> getMapper() {
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

    /**
     * Computes the receive timeout of an input like a socket-based SUL does.
     * It is the response wait, extended by the input's extended wait,
     * or replaced by the input's configured response timeout.
     */
    private long receiveTimeout(MessageInput input) {
        long responseWait = sulConfig.getResponseWait();
        if (input.getExtendedWait() != null) {
            return responseWait + input.getExtendedWait();
        }
        if (sulConfig.getInputResponseTimeout() != null
            && sulConfig.getInputResponseTimeout().containsKey(input.getName())) {
            return sulConfig.getInputResponseTimeout().get(input.getName());
        }
        return responseWait;
    }

    private void consumeInitialClientMessage() {
        context.setReceiveTimeout(INITIAL_MESSAGE_TIMEOUT);
        MessageOutput output = mapper.getOutputMapper().receiveOutput(context);
        if (!mapper.getOutputChecker().hasInitialClientMessage(output)) {
            throw new IllegalStateException("Expected an initial client message, received " + output.getName());
        }
    }
}
