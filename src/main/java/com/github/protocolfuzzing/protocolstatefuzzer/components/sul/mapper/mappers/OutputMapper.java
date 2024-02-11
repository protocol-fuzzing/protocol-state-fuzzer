package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
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
 * Also there are operations such as coalescing outputs into one or splitting an
 * output into its atoms.
 * <p>
 * The contained OutputBuilder is used to create special symbols or create
 * output symbols after coalescing two outputs.
 *
 * @param <S>  the type of execution context's state
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 */
public abstract class OutputMapper<S, I, O extends MapperOutput<O, P>, P> {

    /** Stores the constructor parameter. */
    protected MapperConfig mapperConfig;

    /** Stores the constructor parameter. */
    protected OutputBuilder<O> outputBuilder;

    /** Stores the constructor parameter. */
    protected OutputChecker<O> outputChecker;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param mapperConfig   the configuration of the Mapper
     * @param outputBuilder  the builder of the output symbols
     * @param outputChecker  the checker of the output symbols
     */
    public OutputMapper(MapperConfig mapperConfig, OutputBuilder<O> outputBuilder, OutputChecker<O> outputChecker) {
        this.mapperConfig = mapperConfig;
        this.outputBuilder = outputBuilder;
        this.outputChecker = outputChecker;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return  the stored value of {@link #mapperConfig}
     */
    public MapperConfig getMapperConfig(){
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #outputBuilder}.
     *
     * @return  the stored value of {@link #outputBuilder}
     */
    public OutputBuilder<O> getOutputBuilder() {
        return outputBuilder;
    }

    /**
     * Receives the response from the SUL and converts it to a corresponding
     * output symbol.
     *
     * @param context  the active execution context holding the protocol-specific state
     * @return         the corresponding output symbol
     */
    public abstract O receiveOutput(ExecutionContext<S, I, O> context);

    /**
     * Returns the timeout symbol according to {@link #outputBuilder}.
     *
     * @return  the timeout symbol according to {@link #outputBuilder}
     */
    public O timeout() {
        return outputBuilder.buildTimeout();
    }

    /**
     * Returns the timeout symbol or the socket closed symbol according to
     * {@link #outputBuilder} respecting the {@link MapperConfig#isSocketClosedAsTimeout()}.
     *
     * @return  the timeout symbol or the socket closed symbol */
    public O socketClosed() {
        if (mapperConfig.isSocketClosedAsTimeout()) {
            return outputBuilder.buildTimeout();
        }
        return outputBuilder.buildSocketClosed();
    }

    /**
     * Returns the timeout symbol or the disabled symbol according to
     * {@link #outputBuilder} respecting the {@link MapperConfig#isSocketClosedAsTimeout()}.
     *
     * @return  the timeout symbol or the disabled symbol
     */
    public O disabled() {
        if (mapperConfig.isDisabledAsTimeout()) {
            return outputBuilder.buildTimeout();
        }
        return outputBuilder.buildDisabled();
    }

    /**
     * Coalesces the messages of two output symbols into one output symbol.
     *
     * @param output1  the first output symbol
     * @param output2  the second output symbol
     * @return         the coalesced output symbol or either one of output1 and
     *                 output2 if the other one is the timeout symbol
     */
    public O coalesceOutputs(O output1, O output2) {
        if (outputChecker.isDisabled(output1)
            || outputChecker.isDisabled(output2)
            || outputChecker.isSocketClosed(output1)
            || outputChecker.isSocketClosed(output2)) {
            throw new RuntimeException(
                "Cannot coalesce " + OutputBuilder.DISABLED + " or "
                + OutputBuilder.SOCKET_CLOSED + " outputs");
        }

        if (outputChecker.isTimeout(output1)) {
            return output2;
        }

        if (outputChecker.isTimeout(output2)) {
            return output1;
        }

        String name;
        List<P> messages = null;

        List<String> absOutputStrings = new ArrayList<>(output1.getAtomicAbstractionStrings(2));
        absOutputStrings.addAll(output2.getAtomicAbstractionStrings(2));
        name = mergeRepeatingMessages(absOutputStrings);

        if (output1.hasMessages() && output2.hasMessages()) {
            messages = new ArrayList<>(output1.getMessages());
            messages.addAll(output2.getMessages());
        }

        return outputBuilder.buildOutput(name, messages);
    }

    /**
     * Merges the repeating messages in a given list of strings that contain abstract
     * symbol name.
     * <p>
     * If a message is repeated in the list of strings, then it is merged into one
     * message appended with {@link MapperOutput#REPEATING_INDICATOR}.
     * <p>
     * In the final string the different messages are separated using {@link MapperOutput#MESSAGE_SEPARATOR}.
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
                    builder.insert(builder.length() - 1, MapperOutput.REPEATING_INDICATOR);
                    skipStar = true;
                }
            } else {
                lastSeen = abstractMessageString;
                skipStar = false;
                builder.append(lastSeen);
                builder.append(MapperOutput.MESSAGE_SEPARATOR);
            }
        }
        return builder.substring(0, builder.length() - 1);
    }
}
