package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;

import java.util.*;

public class OutputStandard implements AbstractOutput {

    /** Map from names to special output symbols. */
    protected static final Map<String, OutputStandard> specialOutputsMap = new HashMap<>();

    /** Indicates whether the SUL process is alive or not. */
    protected boolean alive = true;

    /** List of the received protocol messages associated with this output. */
    protected List<ProtocolMessage> messages;

    protected final String name;

     /**
     * Returns the special output symbol of timeout.
     *
     * @return  the special output symbol of timeout
     */
    public static OutputStandard timeout() {
        return new OutputStandard(TIMEOUT);
    }

    /**
     * Returns the special output symbol of unknown.
     *
     * @return  the special output symbol of unknown
     */
    public static OutputStandard unknown() {
        return new OutputStandard(UNKNOWN_MESSAGE);
    }

    /**
     * Returns the special output symbol of socket closed.
     *
     * @return  the special output symbol of socket closed
     */
    public static OutputStandard socketClosed() {
        return new OutputStandard(SOCKET_CLOSED);
    }

    /**
     * Returns the special output symbol of disabled.
     *
     * @return  the special output symbol of disabled
     */
    public static OutputStandard disabled() {
        return new OutputStandard(DISABLED);
    }

    /**
     * Constructs a new instance from the default super constructor intended for
     * output symbols initializing {@link #messages} to null.
     */
    public OutputStandard() {
        this(null, null);
    }

    /**
     * Constructs a new instance from the given parameter using the corresponding
     * super constructor intended for output symbols initializing {@link #messages} to null.
     *
     * @param name  the output symbol name
     */
    public OutputStandard(String name) {
        this(name, null);
    }

     /**
     * Constructs a new instance from the given parameter using the corresponding
     * super constructor intended for output symbols initializing {@link #messages}
     * to the given ones.
     *
     * @param name      the output symbol name
     * @param messages  the list of received protocol messages
     */
    public OutputStandard(String name, List<ProtocolMessage> messages) {
        this.name = name;
        this.messages = messages;
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
    public List<OutputStandard> getAtomicOutputs() {
        return getAtomicOutputs(1);
    }

    /**
     * Returns a list of output symbols, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     *
     * @param unrollRepeating  the number of times a repeating output should be unrolled
     * @return  the list of output symbol instances
     */
    public List<OutputStandard> getAtomicOutputs(int unrollRepeating) {
        List<OutputStandard> outputs = new ArrayList<>();

        if (isAtomic() && !isRepeating()) {
            outputs.add(this);
            return outputs;
        }

        for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
            OutputStandard output = new OutputStandard(absOutput);
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
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof OutputStandard)) {
            return false;
        }

        OutputStandard that = (OutputStandard) o;
        return Objects.equals(getName(), that.getName())
            && (alive == that.alive)
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
