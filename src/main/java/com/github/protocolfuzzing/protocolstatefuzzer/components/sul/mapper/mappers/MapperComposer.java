package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapperComposer implements Mapper {
    private static final Logger LOGGER = LogManager.getLogger();
    protected InputMapper inputMapper;
    protected OutputMapper outputMapper;

    public MapperComposer(InputMapper inputMapper, OutputMapper outputMapper) {
        this.inputMapper = inputMapper;
        this.outputMapper = outputMapper;
    }

    public InputMapper getInputMapper() {
        return inputMapper;
    }

    public OutputMapper getOutputMapper() {
        return outputMapper;
    }

    @Override
    public MapperConfig getMapperConfig(){
        return outputMapper.getMapperConfig();
    }

    @Override
    public AbstractOutputChecker getAbstractOutputChecker() {
        return inputMapper.getOutputChecker();
    }

    @Override
    public AbstractOutput execute(AbstractInput input, ExecutionContext context) {
        LOGGER.info("Executing input symbol {}", input.getName());
        AbstractOutput output;
        context.setInput(input);
        if (context.isExecutionEnabled() && input.isEnabled(context)) {
            output = doExecute(input, context);
        } else {
            output = outputMapper.disabled();
        }
        LOGGER.info("Produced output symbol {}", output.getName());
        return output;
    }

    protected AbstractOutput doExecute(AbstractInput input, ExecutionContext context) {
        inputMapper.sendInput(input, context);
        AbstractOutput output = outputMapper.receiveOutput(context);
        inputMapper.postReceive(input, output, context);
        return output;
    }
}
