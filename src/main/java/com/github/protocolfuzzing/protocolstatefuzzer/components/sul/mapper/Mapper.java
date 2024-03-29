package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * Interface for the Mapper Component, which is responsible for executing an input.
 * <p>
 * Given an input symbol, the mapper should:
 * <ol>
 * <li> convert the input symbol to a protocol message
 * <li> send the protocol message to the SUL
 * <li> receive the protocol message response of the SUL
 * <li> convert the protocol message response to an output symbol
 * </ol>
 */
public interface Mapper {

    /**
     * Executes an input and returns the corresponding output.
     *
     * @param input    the input symbol to be executed
     * @param context  the active execution context
     * @return         the corresponding output symbol
     */
    AbstractOutput execute(AbstractInput input, ExecutionContext context);

    /**
     * Returns the configuration of the Mapper.
     *
     * @return  the configuration of the Mapper
     */
    MapperConfig getMapperConfig();

    /**
     * Returns the instance that checks the output symbols.
     *
     * @return  the instance that checks the output symbols
     */
    AbstractOutputChecker getAbstractOutputChecker();
}
