package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
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
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 * @param <E>  the type of execution context
 * @param <S>  the type of execution context's state
 */
public class MapperComposer<I extends MapperInput<O, P, E>, O extends MapperOutput<O, P>, P, E extends ExecutionContext<I, O, S>, S>
implements Mapper<I, O, E> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected InputMapper<I, O, P, E> inputMapper;

    /** Stores the constructor parameter. */
    protected OutputMapper<O, P, E> outputMapper;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param inputMapper   the InputMapper to be used
     * @param outputMapper  the OutputMapper to be used
     */
    public MapperComposer(InputMapper<I, O, P, E> inputMapper, OutputMapper<O, P, E> outputMapper) {
        this.inputMapper = inputMapper;
        this.outputMapper = outputMapper;
    }

    /**
     * Returns the stored value of {@link #inputMapper}.
     *
     * @return  the stored value of {@link #inputMapper}
     */
    public InputMapper<I, O, P, E> getInputMapper() {
        return inputMapper;
    }

    /**
     * Returns the stored value of {@link #outputMapper}.
     *
     * @return  the stored value of {@link #outputMapper}
     */
    public OutputMapper<O, P, E> getOutputMapper() {
        return outputMapper;
    }

    @Override
    public MapperConfig getMapperConfig(){
        return outputMapper.getMapperConfig();
    }

    /**
     * Returns the OutputChecker contained in the {@link #outputMapper}.
     *
     * @return  the OutputChecker contained in the {@link #outputMapper}
     */
    @Override
    public OutputBuilder<O> getOutputBuilder() {
        return outputMapper.getOutputBuilder();
    }

    /**
     * Returns the OutputChecker contained in the {@link #inputMapper}.
     *
     * @return  the OutputChecker contained in the {@link #inputMapper}
     */
    @Override
    public OutputChecker<O> getOutputChecker() {
        return inputMapper.getOutputChecker();
    }

    @Override
    public O execute(I input, E context) {
        LOGGER.debug("Executing input symbol {}", input.getName());

        O output;

        context.setInput(input);
        if (context.isExecutionEnabled() && input.isEnabled(context)) {
            output = doExecute(input, context);
        } else {
            output = outputMapper.disabled();
        }

        LOGGER.debug("Produced output symbol {}", output.getName());
        return output;
    }

    /**
     * Executes the input symbol using the {@link #inputMapper} and returns the
     * corresponding output symbol using the {@link #outputMapper}.
     *
     * @param input    the input symbol to be executed
     * @param context  the active execution context
     * @return         the corresponding output symbol
     */
    protected O doExecute(I input, E context) {
        inputMapper.sendInput(input, context);
        O output = outputMapper.receiveOutput(context);
        inputMapper.postReceive(input, output, context);
        return output;
    }
}
