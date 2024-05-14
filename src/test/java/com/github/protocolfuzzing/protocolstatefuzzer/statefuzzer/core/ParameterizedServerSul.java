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
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.data.FreshValue;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.words.PSymbolInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A SUL implementing {@link ParameterizedServerRA}
 */
@SuppressWarnings("rawtypes")
public class ParameterizedServerSul extends DataWordSUL
        implements AbstractSul<PSymbolInstance, PSymbolInstance, Object> {
    private static final Integer MAX_MSG_ID = Integer.MAX_VALUE;

    private final Logger LOGGER = LogManager.getLogger();

    private Integer nextMsgId;
    private Random rand;
    private final Map<DataType, Map<DataValue, Object>> buckets = new HashMap<>();

    @Override
    public void pre() {
        buckets.clear();
        nextMsgId = null;
        rand = new Random(1);
    }

    @Override
    public void post() {
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {

        assert in.getBaseSymbol().equals(I_MSG);
        int msgId = (int) in.getParameterValues()[0].getId();

        Stream
                .of(in.getParameterValues())
                .map(this::remapDataValue)
                .toArray(DataValue[]::new);

        if (nextMsgId == null || msgId == nextMsgId) {
            nextMsgId = rand.nextInt(MAX_MSG_ID);

            DataValue dv = new DataValue<>(MSG_ID, nextMsgId);
            DataValue output = remapDataValue(dv);
            LOGGER.info("OUTPUT: " + output);

            return new PSymbolInstance(O_NEXT, output);
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

    @SuppressWarnings("unchecked")
    public DataValue remapDataValue(DataValue dv) {
        Object val = resolve(dv);
        return (isFresh(dv.getType(), val))
                ? registerFreshValue(dv.getType(), val)
                : new DataValue(dv.getType(), val);
    }

    private Object resolve(DataValue d) {
        Map<DataValue, Object> map = this.buckets.get(d.getType());
        if (map == null || !map.containsKey(d)) {
            return d.getId();
        }
        return map.get(d);
    }

    private boolean isFresh(DataType t, Object id) {
        Map<DataValue, Object> map = this.buckets.get(t);
        return map == null || !map.containsValue(id);
    }

    @SuppressWarnings("unchecked")
    private DataValue registerFreshValue(DataType retType, Object ret) {
        Map<DataValue, Object> map = this.buckets.get(retType);
        if (map == null) {
            map = new HashMap<>();
            this.buckets.put(retType, map);
        }

        DataValue v = new DataValue(retType, map.size());
        map.put(v, ret);
        return new FreshValue(v.getType(), v.getId());
    }
}
