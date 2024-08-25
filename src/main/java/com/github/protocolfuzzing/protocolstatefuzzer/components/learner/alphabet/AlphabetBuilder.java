package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.AlphabetProvider;
import net.automatalib.alphabet.Alphabet;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for alphabet operations on different file types.
 *
 * @param <I>  the type of inputs
 */
public interface AlphabetBuilder<I> {

    /** Default alphabet filename without extension that should be in resources. */
    String DEFAULT_ALPHABET_NO_EXTENSION = "default_alphabet";

    /**
     * Builds the (input) alphabet from the given provider.
     *
     * @param alphabetProvider  the provider of the alphabet
     * @return                  the built input alphabet
     */
    Alphabet<I> build(AlphabetProvider alphabetProvider);

    /**
     * Returns a new input stream of the alphabet file specified in the provider
     * or a new input stream of the default alphabet file located in the resources
     * if a null provider or a provider with null alphabet file is provided.
     *
     * @param alphabetProvider  the provider of the alphabet
     * @return                  the file's input stream
     */
    InputStream getAlphabetFileInputStream(AlphabetProvider alphabetProvider);

    /**
     * Returns the alphabet file extension that the builder handles.
     *
     * @return  the alphabet file extension that the builder handles
     */
    String getAlphabetFileExtension();

    /**
     * Exports the given alphabet to the specified file.
     *
     * @param outputFileName  the name of the destination file
     * @param alphabet        the alphabet to be exported
     *
     * @throws IOException                  if an error occurs regarding the destination file
     * @throws AlphabetSerializerException  if an error occurs regarding the alphabet serialization
     */
    void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet)
        throws IOException, AlphabetSerializerException;
}
