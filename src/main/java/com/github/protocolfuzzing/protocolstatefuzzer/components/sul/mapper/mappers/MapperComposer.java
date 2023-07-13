package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of the {@link Mapper} that is comprised of
 * the {@link InputMapper} and the {@link OutputMapper}.
 */
public class MapperComposer implements Mapper {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected InputMapper inputMapper;

    /** Stores the constructor parameter. */
    protected OutputMapper outputMapper;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param inputMapper   the InputMapper to be used
     * @param outputMapper  the OutputMapper to be used
     */
    public MapperComposer(InputMapper inputMapper, OutputMapper outputMapper) {
        this.inputMapper = inputMapper;
        this.outputMapper = outputMapper;
    }

    /**
     * Returns the stored value of {@link #inputMapper}.
     *
     * @return  the stored value of {@link #inputMapper}
     */
    public InputMapper getInputMapper() {
        return inputMapper;
    }

    /**
     * Returns the stored value of {@link #outputMapper}.
     *
     * @return  the stored value of {@link #outputMapper}
     */
    public OutputMapper getOutputMapper() {
        return outputMapper;
    }

    @Override
    public MapperConfig getMapperConfig(){
        return outputMapper.getMapperConfig();
    }

    /**
     * Returns the AbstractOutputChecker contained in the {@link #inputMapper}.
     *
     * @return  the AbstractOutputChecker contained in the {@link #inputMapper}
     */
    @Override
    public AbstractOutputChecker getAbstractOutputChecker() {
        return inputMapper.getOutputChecker();
    }

    @Override
    public AbstractOutput execute(AbstractInput input, ExecutionContext context) {
        LOGGER.debug("Executing input symbol {}", input.getName());

        AbstractOutput output;

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
    protected AbstractOutput doExecute(AbstractInput input, ExecutionContext context) {
        inputMapper.sendInput(input, context);
        AbstractOutput output = outputMapper.receiveOutput(context);
        inputMapper.postReceive(input, output, context);
        return output;
    }
}
