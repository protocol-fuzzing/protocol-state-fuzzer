package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;


/**
 * The output mapper performs the following functions:
 * <ol>
 *  <li> receives the response from SUL over the wire </li>
 *  <li> processes the response by: </li>
 *  <ul>
 *      <li> updating the internal state; </li>
 *      <li> converting response to a corresponding AbstractOutput. </li>
 *  </ul>
 * </ol>
 * <p>
 * Everything to do with how a response is converted into an AbstractOutput should be here.
 * Also implemented are operations over the mapper such as coalescing an output into one or splitting an
 * output into its atoms.
 */
public abstract class OutputMapper {
    private static final Logger LOGGER = LogManager.getLogger();

    /*
     * The minimum number of alert/unknown messages before decryption failure is established.
     */
    protected static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 2;

    /*
     * The minimum number of times an output has to be generated for the repeating output to be used.
     * Note that 2 is the only value currently supported.
     */
    protected static final int MIN_REPEATS_FOR_REPEATING_OUTPUT = 2;

    protected MapperConfig mapperConfig;

    public OutputMapper(MapperConfig mapperConfig) {
        this.mapperConfig = mapperConfig;
    }

    public MapperConfig getMapperConfig(){
        return mapperConfig;
    }

    public abstract AbstractOutput receiveOutput(ExecutionContext context);

    public AbstractOutput timeout() {
        return AbstractOutput.timeout();
    }

    public AbstractOutput socketClosed() {
        if (mapperConfig.isSocketClosedAsTimeout()) {
            return AbstractOutput.timeout();
        } else {
            return AbstractOutput.socketClosed();
        }
    }

    public AbstractOutput disabled() {
        if (mapperConfig.isDisabledAsTimeout()) {
            return AbstractOutput.timeout();
        } else {
            return AbstractOutput.disabled();
        }
    }

    public AbstractOutput coalesceOutputs(AbstractOutput output1, AbstractOutput output2) {
        if (output1.isDisabled() || output2.isDisabled() || output1.isSocketClosed() || output2.isSocketClosed()) {
            throw new RuntimeException("Cannot coalesce " + AbstractOutput.DISABLED + " or "
                    + AbstractOutput.SOCKET_CLOSED + " outputs");
        }

        if (output1.isTimeout()) {
            return output2;
        }

        if (output2.isTimeout()) {
            return output1;
        }

        String abstraction;
        List<ProtocolMessage> messages = null;

        assert (output1.isRecordResponse() && output2.isRecordResponse());

        List<String> absOutputStrings = new LinkedList<>(output1.getAtomicAbstractionStrings(2));
        absOutputStrings.addAll(output2.getAtomicAbstractionStrings(2));
        abstraction = mergeRepeatingMessages(absOutputStrings);

        if (output1.hasMessages() && output2.hasMessages()) {
            messages = new LinkedList<>(output1.getMessages());
            messages.addAll(output2.getMessages());
        }

        return new AbstractOutput(abstraction, messages);
    }

    public List<AbstractOutput> getAtomicOutputs(AbstractOutput output) {
        int minRepeats = mapperConfig.isMergeRepeating() ? MIN_REPEATS_FOR_REPEATING_OUTPUT : Integer.MAX_VALUE;
        return output.getAtomicOutputs(minRepeats);
    }

    protected String mergeRepeatingMessages(List<String> abstractMessageStrings) {
        // in case we find repeated occurrences of types of messages, we coalesce them under +,
        // since some implementations may repeat/retransmit the same message an arbitrary number of times.
        StringBuilder builder = new StringBuilder();
        String lastSeen = null;
        boolean skipStar = false;

        for (String abstractMessageString : abstractMessageStrings) {
            if (lastSeen != null && lastSeen.equals(abstractMessageString) && mapperConfig.isMergeRepeating()) {
                if (!skipStar) {
                    // insert before ,
                    builder.insert(builder.length() - 1, AbstractOutput.REPEATING_INDICATOR);
                    skipStar = true;
                }
            } else {
                lastSeen = abstractMessageString;
                skipStar = false;
                builder.append(lastSeen);
                builder.append(AbstractOutput.MESSAGE_SEPARATOR);
            }
        }
        return builder.substring(0, builder.length() - 1);
    }
}
