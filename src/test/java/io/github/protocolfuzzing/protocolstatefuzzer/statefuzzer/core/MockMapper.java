package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

public class MockMapper implements Mapper<PSymbolInstance, PSymbolInstance, Object> {

    private OutputBuilder<PSymbolInstance> outputBuilder;
    private OutputChecker<PSymbolInstance> outputChecker;

    public MockMapper() {
        outputBuilder = new OutputBuilder<PSymbolInstance>() {
            @Override
            public PSymbolInstance buildOutputExact(String name) {
                return null;
            }
        };

        outputChecker = new OutputChecker<PSymbolInstance>() {
            @Override
            public boolean hasInitialClientMessage(PSymbolInstance output) {
                return false;
            }

            @Override
            public boolean isTimeout(PSymbolInstance output) {
                return false;
            }

            @Override
            public boolean isUnknown(PSymbolInstance output) {
                return false;
            }

            @Override
            public boolean isSocketClosed(PSymbolInstance output) {
                return false;
            }

            @Override
            public boolean isDisabled(PSymbolInstance output) {
                return false;
            }

        };
    }

    @Override
    public PSymbolInstance execute(PSymbolInstance input, Object context) {
        return null;
    }

    @Override
    public MapperConfig getMapperConfig() {
        return new MapperConfig() {};
    }

    @Override
    public OutputBuilder<PSymbolInstance> getOutputBuilder() {
        return outputBuilder;
    }

    @Override
    public OutputChecker<PSymbolInstance> getOutputChecker() {
        return outputChecker;
    }
}
