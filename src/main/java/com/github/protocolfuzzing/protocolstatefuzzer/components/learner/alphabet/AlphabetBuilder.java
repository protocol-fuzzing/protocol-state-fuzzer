package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Interface for alphabet operations on different file types.
 *
 * @param <I> the type of inputs
 */
public interface AlphabetBuilder<I> {

    /** Default alphabet filename without extension that should be in resources. */
    String DEFAULT_ALPHABET_NO_EXTENSION = "default_alphabet";

    /**
     * Builds the (input) alphabet from the given provider.
     *
     * @param  learnerConfig the LearnerConfig containing the alphabet filename
     *
     * @return               the built input alphabet
     */
    Alphabet<I> build(LearnerConfig learnerConfig);

    /**
     * Returns a new input stream of the alphabet file specified in the provider
     * or a new input stream of the default alphabet file located in the resources
     * if a null provider or a provider with null alphabet file is provided.
     *
     * @param  learnerConfig the LearnerConfig containing the alphabet filename
     *
     * @return               the file's input stream
     */
    InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig);

    /**
     * Returns the alphabet file extension that the builder handles.
     *
     * @return the alphabet file extension that the builder handles
     */
    String getAlphabetFileExtension();

    /**
     * Exports the given alphabet to the specified file.
     *
     * @param  outputFileName              the name of the destination file
     * @param  alphabet                    the alphabet to be exported
     *
     * @throws IOException                 if an error occurs regarding the destination file
     * @throws AlphabetSerializerException if an error occurs regarding the alphabet serialization
     */
    void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet)
        throws IOException, AlphabetSerializerException;

    /**
     * Converts the given alphabet to a {@code String} alphabet.
     *
     * @param  alphabet the alphabet to convert
     *
     * @return          a new {@link Alphabet} with each input mapped to its string representation
     */
    default Alphabet<String> toStringAlphabet(Alphabet<I> alphabet) {
        return new ListAlphabet<>(alphabet.stream().map(i -> i.toString()).collect(Collectors.toList()));
    }
}
