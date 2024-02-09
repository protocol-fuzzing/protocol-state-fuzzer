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
public class AbstractOutput extends AbstractSymbol implements MapperOutput<AbstractOutput> {

    /** Indicates whether the SUL process is alive or not. */
    protected boolean alive = true;

    /** List of the received protocol messages associated with this output. */
    protected List<ProtocolMessage> messages;

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
     * Returns the stored value of {@link #messages}.
     *
     * @return  the stored value of {@link #messages}
     */
    @Override
    public List<ProtocolMessage> getMessages() {
        return messages;
    }

    /**
     * Indicates whether the output also contains the concrete {@link #messages}
     * from which the abstraction was derived.
     *
     * @return  {@code true} if {@link #messages} are not null or empty
     */
    @Override
    public boolean hasMessages() {
        return messages != null && !messages.isEmpty();
    }

    @Override
    public boolean isComposite() {
        return getName().contains(MESSAGE_SEPARATOR);
    }

    @Override
    public boolean isAtomic() {
        return !isComposite();
    }

    @Override
    public List<AbstractOutput> getAtomicOutputs() {
        return getAtomicOutputs(1);
    }

    @Override
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

    @Override
    public List<String> getAtomicAbstractionStrings() {
        return getAtomicAbstractionStrings(1);
    }

    @Override
    public List<String> getAtomicAbstractionStrings(int unrollRepeating) {
        String[] atoms = getName().split("\\" + MESSAGE_SEPARATOR, -1);
        List<String> newAtoms = new ArrayList<>();
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

    @Override
    public boolean isRepeating() {
        return isAtomic() && getName().endsWith(REPEATING_INDICATOR);
    }

    @Override
    public AbstractOutput getRepeatedOutput() {
        if (isRepeating()) {
            return new AbstractOutput(getName().substring(0, getName().length() - 1));
        }
        return this;
    }

    /**
     * Returns the stored value of {@link #alive}.
     *
     * @return  the stored value of {@link #alive}
     */
    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets the value of {@link #alive}.
     *
     * @param alive  {@code true} if the SUL process is still alive
     */
    @Override
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
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
