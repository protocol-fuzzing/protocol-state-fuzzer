package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.I_MSG;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.MSG_ID;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.O_ACK;
import static com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServerRA.O_TIMEOUT;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServer.Ack;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.ParameterizedServer.Msg;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.data.FreshValue;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.words.PSymbolInstance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A SUL implementing {@link ParameterizedServerRA}
 */
public class ParameterizedServerSul extends DataWordSUL
        implements AbstractSul<PSymbolInstance, PSymbolInstance, Object> {

    private final Map<DataType, Map<DataValue, BigDecimal>> buckets = new HashMap<>();
    private ParameterizedServer server;

    @Override
    public void pre() {
        buckets.clear();
        server = new ParameterizedServer();
    }

    @Override
    public void post() {
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {

        assert in.getBaseSymbol().equals(I_MSG);

        DataValue[] values = Stream
                .of(in.getParameterValues())
                .map(this::remapDataValue)
                .toArray(DataValue[]::new);

        Ack ack = server.send(new Msg(values[0].getValue()));
        if (ack != null) {
            DataValue dv = new DataValue(MSG_ID, ack.nextMsgId());
            DataValue output = remapDataValue(dv);
            return new PSymbolInstance(O_ACK, output);
        }

        return new PSymbolInstance(O_TIMEOUT);
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

    public DataValue remapDataValue(DataValue dv) {
        BigDecimal val = resolve(dv);

        if (isFresh(dv.getDataType(), val)) {
            return registerFreshValue(dv.getDataType(), val);
        }

        return new DataValue(dv.getDataType(), val);
    }

    private BigDecimal resolve(DataValue dv) {
        Map<DataValue, BigDecimal> map = buckets.get(dv.getDataType());

        if (map == null || !map.containsKey(dv)) {
            return dv.getValue();
        }

        return map.get(dv);
    }

    private boolean isFresh(DataType dt, BigDecimal val) {
        Map<DataValue, BigDecimal> map = buckets.get(dt);
        return map == null || !map.containsValue(val);
    }

    private DataValue registerFreshValue(DataType dt, BigDecimal val) {
        Map<DataValue, BigDecimal> map = buckets.get(dt);
        if (map == null) {
            map = new HashMap<>();
            buckets.put(dt, map);
        }

        DataValue dv = new DataValue(dt, new BigDecimal(map.size()));
        map.put(dv, val);

        return new FreshValue(dv.getDataType(), dv.getValue());
    }
}
