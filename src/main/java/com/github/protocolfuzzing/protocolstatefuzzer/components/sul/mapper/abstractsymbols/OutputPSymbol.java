package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

import java.util.*;

public class OutputPSymbol extends PSymbolInstance implements AbstractOutput {

    /** Map from names to special output symbols. */
    protected static final Map<String, OutputPSymbol> specialOutputsMap = new HashMap<>();

    /** Indicates whether the SUL process is alive or not. */
    protected boolean alive = true;

    /** List of the received protocol messages associated with this output. */
    protected List<ProtocolMessage> messages;

    protected final String name;

    // TODO: Should messages be a constructor parameter?
    public OutputPSymbol(ParameterizedSymbol baseSymbol, DataValue... pValues) {
        super(baseSymbol, pValues);
        this.name = baseSymbol.getName();
    }

    public String getName(){
        return this.name;
    }

    public boolean isInput(){
        return false;
    }

       /**
     * Returns the repeating output of the message if {@link #isRepeating()} or
     * this instance.
     *
     * @return  the repeating output of the message if {@link #isRepeating()} or this instance
     */
    public AbstractOutput getRepeatedOutput() {
        if (isRepeating()) {
            return new OutputStandard(getName().substring(0, getName().length() - 1));
        }
        return this;
    }


    /**
     * Indicates whether the output represents a record response from the SUL,
     * so {@code false} means that the output is timeout or crash or disabled.
     *
     * @return  {@code false} if the output is timeout or crash or disabled
     */
    public boolean isRecordResponse() {
        return (messages != null && !messages.isEmpty()) || (!this.isTimeout() && !this.isDisabled());
    }

    /**
     * Indicates whether the output also contains the concrete {@link #messages}
     * from which the abstraction was derived.
     *
     * @return  {@code true} if {@link #messages} are not null or empty
     */
    public boolean hasMessages() {
        return messages != null && !messages.isEmpty();
    }

        /**
     * Returns a list of output symbols, one for each individual message in the output,
     * unrolling repeating messages only one time.
     *
     * @return  the list of output symbol instances
     */
    public List<AbstractOutput> getAtomicOutputs() {
        return getAtomicOutputs(1);
    }

    /**
     * Returns a list of output symbols, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     *
     * @param unrollRepeating  the number of times a repeating output should be unrolled
     * @return  the list of output symbol instances
     */
    public List<AbstractOutput> getAtomicOutputs(int unrollRepeating) {
        List<AbstractOutput> outputs = new ArrayList<>();

        if (isAtomic() && !isRepeating()) {
            outputs.add(this);
            return outputs;
        }

        for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
            AbstractOutput output = new OutputStandard(absOutput);
            outputs.add(output);
        }
        return outputs;
    }

        /**
     * Returns the content information of this output symbol.
     *
     * @return  the content string of this output symbol
     */
    protected String buildContentInfo() {
        StringBuilder builder = new StringBuilder();
        LinkedHashMap<String, String> printMap = new LinkedHashMap<>();

        printMap.put("isAlive", Boolean.toString(isAlive()));

        if (hasMessages()) {
            printMap.put("messages", messages.toString());
        }

        builder.append("{");
        for (Map.Entry<String, String> entry : printMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            builder.append(";");
        }
        builder.append("}");

        return builder.toString();
    }

    /**
     * Returns the stored value of {@link #messages}.
     *
     * @return  the stored value of {@link #messages}
     */
    public List<ProtocolMessage> getMessages() {
        return messages;
    }

        /**
     * Returns a detailed string of this output symbol that contains
     * the header and the contents.
     *
     * @return  the detailed string with header and contents
     */
    public String toDetailedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append(buildContentInfo());
        return builder.toString();
    }

        /**
     * Returns the stored value of {@link #alive}.
     *
     * @return  the stored value of {@link #alive}
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets the value of {@link #alive}.
     *
     * @param alive  {@code true} if the SUL process is still alive
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Overrides the default method.
     *
     * @return  {@code true} if this instance equals the given object
     */
    // TODO: Compare PSIs
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof OutputPSymbol)) {
            return false;
        }

        OutputPSymbol that = (OutputPSymbol) o;
        return super.equals(that)
            && Objects.equals(messages, that.messages);
    }

     /**
     * Overrides the default method.
     *
     * @return  the hash code of this instance
     */
    @Override
    public int hashCode() {
        return 2 * getName().hashCode();
    }

}
