package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.I_MSG;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.MSG_ID;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.O_NEXT;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.O_TIMEOUT;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.Random;

/**
 * A SUL implementing {@link ParameterizedServerRA}
 */
public class ParameterizedServerSul  extends DataWordSUL implements AbstractSul<PSymbolInstance, PSymbolInstance, Object> {
    private static final Integer MAX_MSG_ID = Integer.MAX_VALUE;

    private Integer nextMsgId;
    private Random rand;

    @Override
    public void pre() {
        nextMsgId = null;
        rand = new Random(1);
    }

    @Override
    public void post() {
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {
        assert(in.getBaseSymbol() == I_MSG);
        Integer msgId = (Integer) in.getParameterValues()[0].getId();
        if (nextMsgId == null || msgId == nextMsgId) {
            nextMsgId = rand.nextInt(MAX_MSG_ID);
            return new PSymbolInstance(O_NEXT, new DataValue<>(MSG_ID, nextMsgId));
        } else {
            return new PSymbolInstance(O_TIMEOUT);
        }
    }

    @Override
    public SulConfig getSulConfig() {
        return new SulServerConfigStandard();
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
        return new MockMapper();
    }

    @Override
    public SulAdapter getSulAdapter() {
        return null;
    }
}
