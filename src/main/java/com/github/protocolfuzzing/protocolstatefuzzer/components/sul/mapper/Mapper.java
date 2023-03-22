package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

/**
 * The mapper component is responsible with executing an input.
 * Given an input symbol, the mapper should:
 * <ol>
 * 	<li> generate a corresponding packet </li>
 * 	<li> send it to the SUL </li>
 * 	<li> receive the response </li>
 * 	<li> convert it into an appropriate response </li>
 * </ol>
 *
 */
public interface Mapper {
	AbstractOutput execute(AbstractInput input, ExecutionContext context);
	MapperConfig getMapperConfig();
	AbstractOutputChecker getAbstractOutputChecker();
}
