package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;

import java.util.ArrayList;
import java.util.List;


/**
 * It is responsible for receiving protocol messages from the SUL and for
 * the concrete-to-abstract function of the Mapper.
 * <p>
 * It performs the following:
 * <ol>
 * <li> Receives the response from the SUL
 * <li> Updates the execution context
 * <li> Converts the response to a corresponding O
 * </ol>
 * <p>
 * It contains everything related to the conversion of a response to an O.
 * Also there are operations such as coalescing an output into one or splitting an
 * output into its atoms.
 */
public abstract class OutputMapper<S, I, O extends AbstractOutput> {

    /** The minimum number of alert/unknown messages before decryption failure is established. */
    protected static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 2;

    /**
     * The minimum number of times an output has to be generated for the repeating output to be used.
     */
    protected static final int MIN_REPEATS_FOR_REPEATING_OUTPUT = 2;

    /** Stores the constructor parameter. */
    protected MapperConfig mapperConfig;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param mapperConfig  the configuration of the Mapper
     */
    public OutputMapper(MapperConfig mapperConfig) {
        this.mapperConfig = mapperConfig;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     * @return  the stored value of {@link #mapperConfig}
     */
    public MapperConfig getMapperConfig(){
        return mapperConfig;
    }

    /**
     * Receives the response from the SUL and converts it to a corresponding
     * output symbol.
     *
     * @param context  the active execution context holding the protocol-specific state
     * @return         the corresponding output symbol
     */
    public abstract O receiveOutput(ExecutionContext<S, I> context);

    /**
     * Returns the default timeout symbol of {@link AbstractOutput#TIMEOUT}.
     *
     * @return  the default timeout symbol of {@link AbstractOutput#TIMEOUT}
     */
    public AbstractOutput timeout() {
        return AbstractOutput.timeout();
    }

    /**
     * Returns the default socket closed symbol of {@link AbstractOutput#SOCKET_CLOSED},
     * respecting the {@link MapperConfig#isSocketClosedAsTimeout()}.
     *
     * @return  the default timeout symbol or the default socket closed symbol
     */
    public AbstractOutput socketClosed() {
        if (mapperConfig.isSocketClosedAsTimeout()) {
            return AbstractOutput.timeout();
        }
        return AbstractOutput.socketClosed();
    }

    /**
     * Returns the default disabled symbol of {@link AbstractOutput#DISABLED},
     * respecting the {@link MapperConfig#isDisabledAsTimeout()}.
     *
     * @return  the default timeout symbol or the default disabled symbol
     */
    public AbstractOutput disabled() {
        if (mapperConfig.isDisabledAsTimeout()) {
            return AbstractOutput.timeout();
        }
        return AbstractOutput.disabled();
    }

    /**
     * Coalesces the messages of two output symbols into one output symbol.
     *
     * @param output1  the first output symbol
     * @param output2  the second output symbol
     * @return         the coalesced output symbol or either one of output1 and
     *                 output2 if the other one is the timeout symbol
     */
    public AbstractOutput coalesceOutputs(O output1, O output2) {
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

        assert(output1.isRecordResponse() && output2.isRecordResponse());

        List<String> absOutputStrings = new ArrayList<>(output1.getAtomicAbstractionStrings(2));
        absOutputStrings.addAll(output2.getAtomicAbstractionStrings(2));
        abstraction = mergeRepeatingMessages(absOutputStrings);

        if (output1.hasMessages() && output2.hasMessages()) {
            messages = new ArrayList<>(output1.getMessages());
            messages.addAll(output2.getMessages());
        }

        return new AbstractOutput(abstraction, messages);
    }

    /**
     * Returns the list of atomic outputs contained in an output symbol.
     *
     * @param output  the output symbol to be searched
     * @return        the list of atomic outputs
     */
    public List<AbstractOutput> getAtomicOutputs(O output) {
        int minRepeats = mapperConfig.isMergeRepeating() ? MIN_REPEATS_FOR_REPEATING_OUTPUT : Integer.MAX_VALUE;
        return output.getAtomicOutputs(minRepeats);
    }

    /**
     * Merges the repeating messages in a given list of strings that contain abstract
     * symbol name.
     * <p>
     * If a message is repeated in the list of strings, then it is merged into one
     * message appended with {@link AbstractOutput#REPEATING_INDICATOR}.
     * <p>
     * In the final string the different messages are separated using {@link AbstractOutput#MESSAGE_SEPARATOR}.
     *
     * @param abstractMessageStrings  the list of abstract symbol names to be merged
     * @return                        the final string of all the messages in the given list
     *                                with the repeating ones having been merged
     */
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
