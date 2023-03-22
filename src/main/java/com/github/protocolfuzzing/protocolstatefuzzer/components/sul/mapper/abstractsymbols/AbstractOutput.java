package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;

import java.util.*;
import java.util.stream.IntStream;

/**
 * This is the parent class of all output symbols.
 * Because of its extension of {@link AbstractSymbol} the class offers the functionality needed by the learner,
 * but also encapsulates the corresponding concrete messages created during learning.
 */
public class AbstractOutput extends AbstractSymbol {
    public static final String MESSAGE_SEPARATOR = "|";
    public static final String REPEATING_INDICATOR = "+";

    // Special abstract outputs
    public static final String TIMEOUT = "TIMEOUT";
    public static final String UNKNOWN_MESSAGE = "UNKNOWN_MESSAGE";
    public static final String SOCKET_CLOSED = "SOCKET_CLOSED";
    public static final String DISABLED = "DISABLED";
    protected static Map<String, AbstractOutput> specialOutputsMap = new HashMap<>();

    // alive indicates whether the process/connection is alive or was lost
    protected boolean alive = true;

    // concrete list of messages
    protected List<ProtocolMessage> messages;

    public AbstractOutput() {
        super(false);
        this.messages = null;
    }

    public AbstractOutput(String name) {
        super(name, false);
        this.messages = null;
    }

    public AbstractOutput(String name, List<ProtocolMessage> messages) {
        super(name, false);
        this.messages = messages;
    }

    protected static AbstractOutput getSpecialOutput(String outString) {
        if (!specialOutputsMap.containsKey(outString))
            specialOutputsMap.put(outString, new AbstractOutput(outString));
        return specialOutputsMap.get(outString);
    }

    public static AbstractOutput timeout() {
        return getSpecialOutput(TIMEOUT);
    }

    public static AbstractOutput unknown() {
        return getSpecialOutput(UNKNOWN_MESSAGE);
    }

    // fields not used in equals, but as carriers of information as tests are executed

    public static AbstractOutput socketClosed() {
        return getSpecialOutput(SOCKET_CLOSED);
    }

    public static AbstractOutput disabled() {
        return getSpecialOutput(DISABLED);
    }

    /**
     * Only includes the abstract output
     */
    public String toString() {
        return getName();
    }

    /**
     * Includes the output header and output details
     */
    public String toDetailedString() {
        // CLIENT_HELLO{isAlive=...}
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        String contentInfo = buildContentInfo();
        builder.append(contentInfo);
        return builder.toString();
    }


    /**
     * Identifies whether the output was derived from multiple distinct messages.
     */
    public boolean isComposite() {
        return getName().contains(MESSAGE_SEPARATOR);
    }

    /**
     * Identifies whether the output was not derived from multiple distinct messages.
     * This means the output encodes a single message, repeating occurrences of the same message or no message.
     */
    public boolean isAtomic() {
        return !isComposite();
    }

    public boolean isRepeating() {
        return isAtomic() && getName().endsWith(REPEATING_INDICATOR);
    }

    public AbstractOutput getRepeatedOutput() {
        if (isRepeating()) {
            return new AbstractOutput(getName().substring(0, getName().length() - 1));
        }
        return this;
    }

    public boolean isTimeout() {
        return TIMEOUT.equals(getName());
    }

    public boolean isDisabled() {
        return DISABLED.equals(getName());
    }

    public boolean isSocketClosed() {
        return SOCKET_CLOSED.equals(getName());
    }

    /**
     * Indicates whether the output represents a record response from the system.
     * False means the output describes timeout/crash/disabled-ness.
     */
    public boolean isRecordResponse() {
        return (messages != null && !messages.isEmpty()) || (!this.isTimeout() && !this.isDisabled());
    }

    /**
     * Indicates whether the output also contains the concrete messages from which the abstraction was derived
     */
    public boolean hasMessages() {
        return messages != null;
    }

    public List<AbstractOutput> getAtomicOutputs() {
        return getAtomicOutputs(1);
    }

    public List<AbstractOutput> getAtomicOutputs(int unrollRepeating) {
        if (isAtomic() && !isRepeating()) {
            return Collections.singletonList(this);
        } else {
            List<AbstractOutput> outputs = new LinkedList<>();
            for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
                AbstractOutput output = new AbstractOutput(absOutput);
                outputs.add(output);
            }
            return outputs;
        }
    }

    public List<String> getAtomicAbstractionStrings() {
        return getAtomicAbstractionStrings(1);
    }

    /*
     * Returns a list of abstraction strings, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     */
    public List<String> getAtomicAbstractionStrings(int unrollRepeating) {
        String[] atoms = getName().split("\\" + MESSAGE_SEPARATOR);
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

    protected String buildContentInfo() {
        StringBuilder builder = new StringBuilder();

        LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
        printMap.put("isAlive", Boolean.toString(isAlive()));
        if (messages != null && !messages.isEmpty()) {
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
     * Returns the protocol messages associated with the output.
     * Returns null if this output was generated from a specification and does not contain protocol messages.
     */
    public List<ProtocolMessage> getMessages() {
        return messages;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractOutput that = (AbstractOutput) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(alive, that.alive)
                && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return 2 * getName().hashCode();
    }
}
