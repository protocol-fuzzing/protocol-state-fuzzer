package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;

import java.util.HashSet;
import java.util.Set;

/**
 * The execution context of a test. It holds the receive timeout of the current step,
 * and the data values of the connection so far, which tell whether a received
 * value is fresh.
 */
public class RaExecutionContext extends
    ExecutionContextStepped<PSymbolInstance, PSymbolInstance, Void, StepContext<PSymbolInstance, PSymbolInstance>> {

    private final Set<DataValue> values = new HashSet<>();
    private long receiveTimeout;

    public RaExecutionContext() {
        super(null);
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Records a data value of the connection.
     *
     * @param  value the value, sent or received
     *
     * @return       {@code true} if the value is new to the connection
     */
    public boolean addValue(DataValue value) {
        return values.add(new DataValue(value.getDataType(), value.getValue()));
    }

    @Override
    protected StepContext<PSymbolInstance, PSymbolInstance> buildStepContext() {
        return new StepContext<>(stepContexts.size());
    }
}
