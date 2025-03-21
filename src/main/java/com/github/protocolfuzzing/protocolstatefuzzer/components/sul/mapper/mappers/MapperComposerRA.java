package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of the {@link Mapper} that is comprised of
 * the {@link InputMapper} and the {@link OutputMapper}.
 *
 * @param <D> the type of input and output domain
 * @param <P> the type of protocol messages
 * @param <E> the type of execution context
 * @param <S> the type of execution state
 */
public class MapperComposerRA<D, P, E extends ExecutionContext<D, D, S>, S>
        implements Mapper<D, D, E> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected InputMapperRA<D, P, E> inputMapper;

    /** Stores the constructor parameter. */
    protected OutputMapperRA<D, P, E> outputMapper;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param inputMapper  the InputMapper to be used
     * @param outputMapper the OutputMapper to be used
     */
    public MapperComposerRA(InputMapperRA<D, P, E> inputMapper,
            OutputMapperRA<D, P, E> outputMapper) {
        this.inputMapper = inputMapper;
        this.outputMapper = outputMapper;
    }

    /**
     * Returns the stored value of {@link #inputMapper}.
     *
     * @return the stored value of {@link #inputMapper}
     */
    public InputMapperRA<D, P, E> getInputMapper() {
        return inputMapper;
    }

    /**
     * Returns the stored value of {@link #outputMapper}.
     *
     * @return the stored value of {@link #outputMapper}
     */
    public OutputMapperRA<D, P, E> getOutputMapper() {
        return outputMapper;
    }

    @Override
    public MapperConfig getMapperConfig() {
        return outputMapper.getMapperConfig();
    }

    /**
     * Returns the OutputBuilder contained in the {@link #outputMapper}.
     *
     * @return the OutputBuilder contained in the {@link #outputMapper}
     */
    @Override
    public OutputBuilder<D> getOutputBuilder() {
        return outputMapper.getOutputBuilder();
    }

    /**
     * Returns the OutputChecker contained in the {@link #inputMapper}.
     *
     * @return the OutputChecker contained in the {@link #inputMapper}
     */
    @Override
    public OutputChecker<D> getOutputChecker() {
        return inputMapper.getOutputChecker();
    }

    @Override
    public D execute(D input, E context) {
        LOGGER.debug("Executing input symbol {}", input);

        D output;

        context.setInput(input);
        if (context.isExecutionEnabled() && inputMapper.isEnabled(input, context)) {
            output = doExecute(input, context);
        } else {
            output = outputMapper.disabled();
        }

        LOGGER.debug("Produced output symbol {}", output);
        return output;
    }

    /**
     * Executes the input symbol using the {@link #inputMapper} and returns the
     * corresponding output symbol using the {@link #outputMapper}.
     *
     * @param input   the input symbol to be executed
     * @param context the active execution context
     * @return the corresponding output symbol
     */
    protected D doExecute(D input, E context) {
        inputMapper.sendInput(input, context);
        D output = outputMapper.receiveOutput(context);
        inputMapper.postReceive(input, output, context);
        return output;
    }
}
