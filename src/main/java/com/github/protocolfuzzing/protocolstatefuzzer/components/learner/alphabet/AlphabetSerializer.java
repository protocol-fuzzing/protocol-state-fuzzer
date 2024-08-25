package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import net.automatalib.alphabet.Alphabet;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for reading and writing operations for a specific file type of
 * alphabets.
 */
public interface AlphabetSerializer<I> {

    /**
     * Reads the alphabet from the given input stream.
     *
     * @param alphabetStream  the source input stream
     * @return                the alphabet read
     *
     * @throws AlphabetSerializerException  if an error occurs
     */
    Alphabet<I> read(InputStream alphabetStream) throws AlphabetSerializerException;

    /**
     * Writes the given alphabet to the specified output stream.
     *
     * @param alphabetStream  the destination output stream
     * @param alphabet        the alphabet to be written
     *
     * @throws AlphabetSerializerException  if an error occurs
     */
    void write(OutputStream alphabetStream, Alphabet<I> alphabet) throws AlphabetSerializerException;

    /**
     * Returns the file extension that this serializer handles prepended by a . (dot).
     *
     * @return  the file extension that this serializer handles prepended by a . (dot)
     */
    String getAlphabetFileExtension();
}
