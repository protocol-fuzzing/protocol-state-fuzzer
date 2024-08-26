package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An example of implementing the {@link MapperOutput} interface and also extending the
 * {@link AbstractSymbol} class.
 * <p>
 * It also encapsulates the corresponding concrete messages created during learning.
 *
 * @param <O>  the type of outputs
 * @param <P>  the type of protocol messages
 */
public abstract class AbstractOutput<O, P> extends AbstractSymbol implements MapperOutput<O, P> {

    /** List of the received protocol messages associated with this output. */
    protected List<P> messages;

    /**
     * Constructs a new instance and initializes {@link #messages} to null.
     */
    public AbstractOutput() {
        super(false);
        this.messages = null;
    }

    /**
     * Constructs a new instance from the given parameter and
     * initializes {@link #messages} to null.
     *
     * @param name  the output symbol name
     */
    public AbstractOutput(String name) {
        super(name, false);
        this.messages = null;
    }

    /**
     * Constructs a new instance from the given parameters and
     * initializes {@link #messages} to the given ones.
     *
     * @param name      the output symbol name
     * @param messages  the list of received protocol messages
     */
    public AbstractOutput(String name, List<P> messages) {
        super(name, false);
        this.messages = messages;
    }

    /**
     * Returns the stored value of {@link #messages}.
     *
     * @return  the stored value of {@link #messages}
     */
    @Override
    public List<P> getMessages() {
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
    public List<O> getAtomicOutputs() {
        return getAtomicOutputs(1);
    }

    @Override
    public List<O> getAtomicOutputs(int unrollRepeating) {
        List<O> outputs = new ArrayList<>();

        if (isAtomic() && !isRepeating()) {
            outputs.add(this.convertOutput());
            return outputs;
        }

        for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
            O output = buildOutput(absOutput);
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
                for (Integer i = 0; i < unrollRepeating; i++) {
                    newAtoms.add(repeatingAtom);
                }
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
    public O getRepeatedOutput() {
        if (isRepeating()) {
            return buildOutput(getName().substring(0, getName().length() - 1));
        }
        return this.convertOutput();
    }

    /**
     * Builds a new O output given its name.
     *
     * @param name  the name of the output
     * @return      the build O output
     */
    protected abstract O buildOutput(String name);

    /**
     * Converts the current AbstractOutput to an O output.
     *
     * @return  the converted O output
     */
    protected abstract O convertOutput();

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

        AbstractOutput<?, ?> that = AbstractOutput.class.cast(o);

        return Objects.equals(getName(), that.getName())
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
