package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.sul.SimulatorSUL;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.Map;

public class RASUL implements AbstractSUL<PSymbolInstance, PSymbolInstance, Object> {

    private SimulatorSUL sul;

    @SuppressWarnings("rawtypes")
    public RASUL(RegisterAutomaton ra, Map<DataType, Theory> teachers,
            Constants consts) {
        sul = new SimulatorSUL(ra, teachers, consts);
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {
        return sul.step(in);
    }

    @Override
    public SULConfig getSULConfig() {
        return new SULServerConfigStandard();
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return null;
    }

    @Override
    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
    }

    @Override
    public DynamicPortProvider getDynamicPortProvider() {
        return null;
    }

    @Override
    public Mapper<PSymbolInstance, PSymbolInstance, Object> getMapper() {
        return new RAMockMapper();
    }

    @Override
    public SULAdapter getSULAdapter() {
        return null;
    }

    static class RAMockMapper implements Mapper<PSymbolInstance, PSymbolInstance, Object> {

        private OutputBuilder<PSymbolInstance> outputBuilder;
        private OutputChecker<PSymbolInstance> outputChecker;

        RAMockMapper() {
            this.outputBuilder = new OutputBuilder<PSymbolInstance>() {
                @Override
                public PSymbolInstance buildOutputExact(String name) {
                    return null;
                }
            };

            this.outputChecker = new OutputChecker<PSymbolInstance>() {
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
            return new MapperConfig(){};
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
}
