package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols;

import java.util.List;

/**
 * TODO
 */
public interface MapperOutput<O, P> {

    /** Separator of composite output messages that contain many output symbols. */
    static final String MESSAGE_SEPARATOR = "|";

    /** Indicator used to collapse multiple occurrences of the same symbol. */
    static final String REPEATING_INDICATOR = "+";

    /**
     * Returns the name of the input.
     *
     * @return  the name of the input
     */
    String getName();

    /**
     * Returns the stored messages.
     *
     * @return  the stored messages
     */
    List<P> getMessages();

    /**
     * Indicates whether the output also contains the concrete messages
     * from which the abstraction was derived.
     *
     * @return  {@code true} if messages are contained
     */
    boolean hasMessages();

    /**
     * Identifies whether the output was derived from multiple distinct messages.
     *
     * @return  {@code true} if the output contains multiple messages
     */
    boolean isComposite();

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
    boolean isAtomic();

    /**
     * Returns a list of output symbols, one for each individual message in the output,
     * unrolling repeating messages only one time.
     *
     * @return  the list of output symbol instances
     */
    List<O> getAtomicOutputs();

    /**
     * Returns a list of output symbols, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     *
     * @param unrollRepeating  the number of times a repeating output should be unrolled
     * @return  the list of output symbol instances
     */
    List<O> getAtomicOutputs(int unrollRepeating);

    /**
     * Returns a list of abstraction strings, one for each individual message in the output,
     * unrolling repeating messages only one time.
     *
     * @return  the list of output strings
     */
    List<String> getAtomicAbstractionStrings();

    /**
     * Returns a list of abstraction strings, one for each individual message in the output,
     * unrolling repeating messages the given number of times.
     *
     * @param unrollRepeating  the number of times a repeating output should be unrolled
     * @return                 the list of output strings
     */
    List<String> getAtomicAbstractionStrings(int unrollRepeating);

    /**
     * Identifies whether the output contains only a single repeating message.
     *
     * @return  {@code true} if the output is a single repeating message
     */
    boolean isRepeating();

    /**
     * Returns the repeating output of the message if {@link #isRepeating()} or
     * this instance.
     *
     * @return  the repeating output of the message if {@link #isRepeating()} or this instance
     */
    O getRepeatedOutput();

    /**
     * Returns a detailed string of this output symbol that contains
     * the header and the contents.
     *
     * @return  the detailed string with header and contents
     */
    String toDetailedString();
}
