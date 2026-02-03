package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;

import java.io.IOException;
import java.io.InputStream;

/**
 * A builder wrapper for creating {@code EnumAlphabet} as a builder is required
 * for creating an Alphabet.
 *
 * @param <I> the type of inputs
 */
public class AlphabetBuilderEnum<I> implements AlphabetBuilder<I> {

    private Alphabet<I> alphabet;

    /**
     * Constructs a Wrapper from an Alphabet by wrapping it
     *
     * @param alphabet the alphabet to wrap
     */
    public AlphabetBuilderEnum(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Alphabet<I> build(LearnerConfig learnerConfig) {
        return alphabet;
    }


    // The following methods are not needed because EnumAlphabet does not use an alphabet file.
    @Override
    public String getAlphabetFileExtension() {
        return "";
    }

    @Override
    public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
        return InputStream.nullInputStream();
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet)
            throws IOException, AlphabetSerializerException {
        // Do nothing.
    }
}
