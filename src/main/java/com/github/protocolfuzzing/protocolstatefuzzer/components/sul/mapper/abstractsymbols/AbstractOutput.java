package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;

import java.util.*;
import java.util.stream.IntStream;

/**
 * The parent class of all output symbols.
 * <p>
 * By extending the {@link AbstractSymbol} it offers the functionality that the Learner needs
 * and also encapsulates the corresponding concrete messages created during learning.
 */
public class AbstractOutput extends AbstractSymbol {

    /** Separator of composite output messages that contain many output symbols. */
    public static final String MESSAGE_SEPARATOR = "|";

    /** Indicator used to collapse multiple occurrences of the same symbol. */
    public static final String REPEATING_INDICATOR = "+";

    /** Special output symbol to show that no response was received during the waiting time. */
    public static final String TIMEOUT = "TIMEOUT";

    /** Special output symbol to show that the response could not be identified. */
    public static final String UNKNOWN_MESSAGE = "UNKNOWN_MESSAGE";

    /** Special output symbol to show that the SUL process has terminated. */
    public static final String SOCKET_CLOSED = "SOCKET_CLOSED";

    /** Special output symbol to show that the output is disabled. */
    public static final String DISABLED = "DISABLED";

    /** Map from names to special output symbols. */
    protected static final Map<String, AbstractOutput> specialOutputsMap = new HashMap<>();

    /** Indicates whether the SUL process is alive or not. */
    protected boolean alive = true;

    /** List of the received protocol messages associated with this output. */
    protected List<ProtocolMessage> messages;

    /**
     * Returns the special output symbol of timeout.
     *
     * @return  the special output symbol of timeout
     */
    public static AbstractOutput timeout() {
        return getSpecialOutput(TIMEOUT);
    }

    /**
     * Returns the special output symbol of unknown.
     *
     * @return  the special output symbol of unknown
     */
    public static AbstractOutput unknown() {
        return getSpecialOutput(UNKNOWN_MESSAGE);
    }

    /**
     * Returns the special output symbol of socket closed.
     *
     * @return  the special output symbol of socket closed
     */
    public static AbstractOutput socketClosed() {
        return getSpecialOutput(SOCKET_CLOSED);
    }

    /**
     * Returns the special output symbol of disabled.
     *
     * @return  the special output symbol of disabled
     */
    public static AbstractOutput disabled() {
        return getSpecialOutput(DISABLED);
    }

    /**
     * Returns the special output symbol from the {@link #specialOutputsMap}.
     *
     * @param symbolName  the name of the special output symbol
     * @return            the corresponding symbol instance in {@link #specialOutputsMap}
     */
    protected static AbstractOutput getSpecialOutput(String symbolName) {
        if (!specialOutputsMap.containsKey(symbolName)) {
            specialOutputsMap.put(symbolName, new AbstractOutput(symbolName));
        }
        return specialOutputsMap.get(symbolName);
    }

    /**
     * Constructs a new instance from the default super constructor intended for
     * output symbols initializing {@link #messages} to null.
     */
    public AbstractOutput() {
        super(false);
        this.messages = null;
    }

    /**
     * Constructs a new instance from the given parameter using the corresponding
     * super constructor intended for output symbols initializing {@link #messages} to null.
     *
     * @param name  the output symbol name
     */
    public AbstractOutput(String name) {
        super(name, false);
        this.messages = null;
    }

    /**
     * Constructs a new instance from the given parameter using the corresponding
     * super constructor intended for output symbols initializing {@link #messages}
     * to the given ones.
     *
     * @param name      the output symbol name
     * @param messages  the list of received protocol messages
     */
    public AbstractOutput(String name, List<ProtocolMessage> messages) {
        super(name, false);
        this.messages = messages;
    }

    /**
     * Identifies whether the output was derived from multiple distinct messages.
     *
     * @return  {@code true} if the output contains multiple messages
     */
    public boolean isComposite() {
        return getName().contains(MESSAGE_SEPARATOR);
    }

    /**
     * Identifies whether the output was not derived from multiple distinct messages.
     * <p>
     * This means the output can be:
     * <ul>
     * <li> a single message or
     * <li> repeating occurrences of the same message or
     * <li> no message
     * </ul>
     *
     * @return  {@code true} if the output is not composite
     */
    public boolean isAtomic() {
        return !isComposite();
    }

    /**
     * Identifies whether the output contains only a single repeating message.
     *
     * @return  {@code true} if the output is a single repeating message
     */
    public boolean isRepeating() {
        return isAtomic() && getName().endsWith(REPEATING_INDICATOR);
    }

    /**
     * Returns the repeating output of the message if {@link #isRepeating()} or
     * this instance.
     *
     * @return  the repeating output of the message if {@link #isRepeating()} or this instance
     */
    public AbstractOutput getRepeatedOutput() {
        if (isRepeating()) {
            return new AbstractOutput(getName().substring(0, getName().length() - 1));
        }
        return this;
    }

    /**
     * Returns {@code true} if this output is {@link #TIMEOUT}.
     *
     * @return  {@code true} if this output is {@link #TIMEOUT}
     */
    public boolean isTimeout() {
        return TIMEOUT.equals(getName());
    }

    /**
     * Returns {@code true} if this output is {@link #SOCKET_CLOSED}.
     *
     * @return  {@code true} if this output is {@link #SOCKET_CLOSED}
     */
    public boolean isSocketClosed() {
        return SOCKET_CLOSED.equals(getName());
    }

    /**
     * Returns {@code true} if this output is {@link #DISABLED}.
     *
     * @return  {@code true} if this output is {@link #DISABLED}
     */
    public boolean isDisabled() {
        return DISABLED.equals(getName());
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
            AbstractOutput output = new AbstractOutput(absOutput);
            outputs.add(output);
        }
        return outputs;
    }

    /**
     * Returns a list of abstraction strings, one for each individual message in the output,
     * unrolling repeating messages only one time.
     *
     * @return  the list of output strings
     */
    public List<String> getAtomicAbstractionStrings() {
        return getAtomicAbstractionStrings(1);
    }

    /**
     * Returns a list of abstraction strings, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     *
     * @param unrollRepeating  the number of times a repeating output should be unrolled
     * @return                 the list of output strings
     */
    public List<String> getAtomicAbstractionStrings(int unrollRepeating) {
        String[] atoms = getName().split("\\" + MESSAGE_SEPARATOR, -1);
        List<String> newAtoms = new LinkedList<>();
        for (String atom : atoms) {
            if (atom.endsWith(REPEATING_INDICATOR)) {
                String repeatingAtom = atom.substring(0, atom.length() - REPEATING_INDICATOR.length());
                IntStream.range(0, unrollRepeating).forEach(i -> newAtoms.add(repeatingAtom));
            } else {
                newAtoms.add(atom);
            }
        }
        return newAtoms;
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

        if (o == null || !(o instanceof AbstractOutput)) {
            return false;
        }

        AbstractOutput that = (AbstractOutput) o;
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
