package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

public abstract class InputMapper {
    protected MapperConfig mapperConfig;
    protected AbstractOutputChecker outputChecker;

    public InputMapper(MapperConfig mapperConfig, AbstractOutputChecker outputChecker) {
        this.mapperConfig = mapperConfig;
        this.outputChecker = outputChecker;
    }

    public MapperConfig getMapperConfig(){
        return mapperConfig;
    }

    public AbstractOutputChecker getOutputChecker() {
        return outputChecker;
    }

    public void sendInput(AbstractInput input, ExecutionContext context) {
        input.preSendUpdate(context);
		sendMessage(input.generateProtocolMessage(context), context);
		input.postSendUpdate(context);
    }

    protected abstract void sendMessage(ProtocolMessage message, ExecutionContext context);

    public void postReceive(AbstractInput input, AbstractOutput output, ExecutionContext context) {
        input.postReceiveUpdate(output, outputChecker, context);
    }
}
