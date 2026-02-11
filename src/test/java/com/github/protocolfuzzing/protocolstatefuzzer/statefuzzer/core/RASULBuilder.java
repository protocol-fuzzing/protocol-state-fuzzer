package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.Map;

public class RASULBuilder implements SULBuilder<PSymbolInstance, PSymbolInstance, Object> {

    private RegisterAutomaton ra;
    @SuppressWarnings("rawtypes")
    private Map<DataType, Theory> teachers;
    private Constants consts;

    @SuppressWarnings("rawtypes")
    public RASULBuilder(RegisterAutomaton ra, Map<DataType, Theory> teachers,
            Constants consts) {
        this.ra = ra;
        this.teachers = teachers;
        this.consts = consts;
    }

    @Override
    public AbstractSUL<PSymbolInstance, PSymbolInstance, Object> buildSUL(SULConfig sulConfig, CleanupTasks cleanupTasks) {
        return new RASUL(ra, teachers, consts);
    }

    @Override
    public SULWrapper<PSymbolInstance, PSymbolInstance, Object> buildWrapper() {
        return new SULWrapperStandard<>();
    }
}
