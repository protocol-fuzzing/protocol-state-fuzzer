package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;

import java.io.IOException;
import java.io.InputStream;

/**
 * AlphabetBuilder for creating {@code EnumAlphabet}.
 * Provides an already created alphabet object to the {@code StateFuzzerComposer}
 * Contains methods returning dummy values because the functionality is unused.
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
