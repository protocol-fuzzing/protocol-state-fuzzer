package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;

import java.io.IOException;
import java.io.InputStream;

public class AlphabetBuilderWrapper<I> implements AlphabetBuilder<I> {

    private Alphabet<I> alphabet;

    public AlphabetBuilderWrapper(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Alphabet<I> build(LearnerConfig learnerConfig) {
        return alphabet;
    }

    @Override
    public String getAlphabetFileExtension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet)
            throws IOException, AlphabetSerializerException {
        // TODO Auto-generated method stub

    }
}
