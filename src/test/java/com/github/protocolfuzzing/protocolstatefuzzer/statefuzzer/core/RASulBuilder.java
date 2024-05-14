package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.Map;

public class RASulBuilder implements SulBuilder<PSymbolInstance, PSymbolInstance, Object> {

    private RegisterAutomaton ra;
    private Map<DataType, Theory> teachers;
    private Constants consts;

    public RASulBuilder(RegisterAutomaton ra,  Map<DataType, Theory> teachers,
            Constants consts) {
        this.ra = ra;
        this.teachers = teachers;
        this.consts = consts;
    }

    @Override
    public AbstractSul<PSymbolInstance, PSymbolInstance, Object> build(SulConfig sulConfig, CleanupTasks cleanupTasks) {
        return new RASul(ra, teachers, consts);
    }
}
